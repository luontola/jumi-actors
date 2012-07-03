// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.dynamic.*;
import fi.jumi.actors.eventizers.*;
import fi.jumi.actors.failures.*;
import fi.jumi.actors.logging.*;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public abstract class ActorsContract<T extends Actors> extends ActorsContractHelpers<T> {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected final SilentMessageLogger defaultLogger = new SilentMessageLogger();
    protected final SilentFailureHandler defaultFailureHandler = new SilentFailureHandler();
    protected final ComposedEventizerProvider defaultEventizerProvider =
            new ComposedEventizerProvider(
                    new DummyListenerEventizer(),
                    new DynamicEventizer<Runnable>(Runnable.class));

    @Before
    public void initActors() {
        actors = newActors(defaultEventizerProvider, defaultFailureHandler, defaultLogger);
    }

    /**
     * Avoid the thread interrupted status from leaking from one test to another,
     * since some tests in this class do interrupt threads.
     */
    @After
    public void clearThreadInterruptedStatus() {
        Thread.interrupted();
    }

    protected abstract T newActors(EventizerProvider eventizerProvider, FailureHandler failureHandler, MessageLogger logger);


    // event processing

    @Test
    public void method_calls_on_ActorRef_are_forwarded_to_the_actor() throws InterruptedException {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<DummyListener> actorRef = actorThread.bindActor(DummyListener.class, new SpyDummyListener());

        actorRef.tell().onSomething("event parameter");
        awaitEvents(1);

        assertEvents("event parameter");
    }

    @Test
    public void events_to_an_actor_are_processed_in_the_order_they_were_sent() throws InterruptedException {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new SpyDummyListener());

        actor.tell().onSomething("event 1");
        actor.tell().onSomething("event 2");
        actor.tell().onSomething("event 3");
        awaitEvents(3);

        assertEvents("event 1", "event 2", "event 3");
    }


    // threads

    @Test
    public void actors_bound_to_the_same_actor_thread_are_processed_in_the_same_thread() {
        actors = newActors(new DynamicEventizerProvider(), defaultFailureHandler, defaultLogger);
        ActorThread actorThread = actors.startActorThread();

        class Actor1 implements PrimaryInterface {
            public volatile Thread eventThread;

            @Override
            public void onPrimaryEvent() {
                eventThread = Thread.currentThread();
                logEvent("actor1");
            }
        }
        class Actor2 implements SecondaryInterface {
            public volatile Thread eventThread;

            @Override
            public void onSecondaryEvent() {
                eventThread = Thread.currentThread();
                logEvent("actor2");
            }
        }

        Actor1 actor1 = new Actor1();
        ActorRef<PrimaryInterface> ref1 = actorThread.bindActor(PrimaryInterface.class, actor1);
        ref1.tell().onPrimaryEvent();
        awaitEvents(1);

        Actor2 actor2 = new Actor2();
        ActorRef<SecondaryInterface> ref2 = actorThread.bindActor(SecondaryInterface.class, actor2);
        ref2.tell().onSecondaryEvent();
        awaitEvents(2);

        assertEvents("actor1", "actor2");
        assertThat("actor1 actor was called", actor1.eventThread, is(notNullValue()));
        assertThat("actor2 actor was called", actor2.eventThread, is(notNullValue()));
        assertThat("both actors were processed in the same thread", actor1.eventThread, is(actor2.eventThread));
    }

    @Test
    public void when_an_actor_interrupts_itself_then_the_actor_thread_stops_immediately() {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new SpyDummyListener() {
            @Override
            public void onSomething(String parameter) {
                super.onSomething(parameter);
                if (parameter.equals("interrupt")) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        actor.tell().onSomething("before");
        actor.tell().onSomething("interrupt");
        actor.tell().onSomething("after");

        awaitEvents(2);
        expectNoMoreEvents();
        assertEvents("before", "interrupt");
    }

    @Test
    public void an_actor_can_interrupt_itself_also_by_throwing_InterruptedException() {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new SpyDummyListener() {
            @Override
            public void onSomething(String parameter) {
                super.onSomething(parameter);
                if (parameter.equals("interrupt")) {
                    throw SneakyThrow.rethrow(new InterruptedException());
                }
            }
        });

        actor.tell().onSomething("before");
        actor.tell().onSomething("interrupt");
        actor.tell().onSomething("after");

        awaitEvents(2);
        expectNoMoreEvents();
        assertEvents("before", "interrupt");
    }

    @Test
    public void when_actor_thread_is_stopped_then_it_stops_after_processing_previously_sent_events() {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new SpyDummyListener());

        actor.tell().onSomething("before");
        actorThread.stop();
        actor.tell().onSomething("after");

        awaitEvents(1);
        expectNoMoreEvents();
        assertEvents("before");
    }

    @Test
    public void stopping_one_actor_thread_does_not_affect_unrelated_actor_threads() {
        ActorThread stoppedThread = actors.startActorThread();
        ActorThread unrelatedThread = actors.startActorThread();
        ActorRef<DummyListener> unrelatedActor = unrelatedThread.bindActor(DummyListener.class, new SpyDummyListener());

        stoppedThread.stop();
        unrelatedActor.tell().onSomething("unrelated message");

        awaitEvents(1);
        assertEvents("unrelated message");
    }


    // failure handling

    @Test
    public void exceptions_thrown_by_actors_are_given_to_the_FailureHandler() {
        SpyFailureHandler failureHandler = new SpyFailureHandler();
        DummyExceptionThrowingActor throwerActor = new DummyExceptionThrowingActor();
        ActorRef<DummyListener> actor = bindActorWithFailureHandler(failureHandler, throwerActor);

        actor.tell().onSomething("");
        awaitEvents(1);

        assertThat(failureHandler.lastActor, is((Object) throwerActor));
        assertThat(failureHandler.lastException, is(throwerActor.thrownException));
    }

    @Test
    public void also_InterruptedExceptions_are_given_to_the_FailureHandler() {
        SpyFailureHandler failureHandler = new SpyFailureHandler();
        DummyExceptionThrowingActor throwerActor = new DummyExceptionThrowingActor(InterruptedException.class, "dummy InterruptedException");
        ActorRef<DummyListener> actor = bindActorWithFailureHandler(failureHandler, throwerActor);

        actor.tell().onSomething("");
        awaitEvents(1);

        assertThat(failureHandler.lastException, is(instanceOf(InterruptedException.class)));
        assertThat(failureHandler.lastException, is(throwerActor.thrownException));
    }

    private ActorRef<DummyListener> bindActorWithFailureHandler(FailureHandler failureHandler, DummyListener rawActor) {
        T actors = newActors(defaultEventizerProvider, failureHandler, defaultLogger);
        ActorThread actorThread = actors.startActorThread();
        return actorThread.bindActor(DummyListener.class, rawActor);
    }

    // TODO: actor should stay alive, process following messages
    // TODO: how to stop actors which throw exceptions


    // message logging

    @Test
    public void sending_and_processing_messages_is_logged() {
        MessageLogger logger = mock(MessageLogger.class);
        actors = newActors(defaultEventizerProvider, defaultFailureHandler, logger);
        ActorThread actorThread = actors.startActorThread();
        DummyListener rawActor = mock(DummyListener.class);
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, rawActor);

        actor.tell().onSomething("parameter");
        sendSyncEvent(actorThread);                 // wait for onProcessingFinished to be called
        awaitEvents(1);

        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).onMessageSent(new OnSomethingEvent("parameter"));
        inOrder.verify(logger).onProcessingStarted(rawActor, new OnSomethingEvent("parameter"));
        inOrder.verify(logger).onProcessingFinished();
    }
}
