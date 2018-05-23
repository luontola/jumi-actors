// Copyright © 2011-2018, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizer;
import fi.jumi.actors.listeners.*;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public abstract class ActorsContract<T extends Actors> extends ActorsContractHelpers<T> {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected final NullMessageListener defaultMessageListener = new NullMessageListener();
    protected final UncaughtExceptionCollector defaultFailureHandler = new UncaughtExceptionCollector();
    protected final ComposedEventizerProvider defaultEventizerProvider =
            new ComposedEventizerProvider(
                    new DummyListenerEventizer(),
                    new DynamicEventizer<>(PrimaryInterface.class),
                    new DynamicEventizer<>(SecondaryInterface.class),
                    new DynamicEventizer<>(ResultsInterface.class),
                    new DynamicEventizer<>(Runnable.class));

    @Before
    public void initActors() {
        actors = newActors(defaultEventizerProvider, defaultFailureHandler, defaultMessageListener);
    }

    /**
     * Avoid the thread interrupted status from leaking from one test to another, since some tests in this class do
     * interrupt threads.
     */
    @After
    public void clearThreadInterruptedStatus() {
        Thread.interrupted();
    }

    @After
    public void checkNoUncaughtExceptions() {
        defaultFailureHandler.failIfNotEmpty();
    }

    protected abstract T newActors(EventizerProvider eventizerProvider, FailureHandler failureHandler, MessageListener messageListener);


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


    // promises

    @Test
    public void handlers_can_return_already_satisfied_Promises() {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<ResultsInterface> actor = actorThread.bindActor(ResultsInterface.class, new ResultsAdapter() {
            @Override
            public Promise<String> returnsPromise() {
                logEvent("event 1");
                return Promise.of("return value");
            }
        });

        actor.tell().returnsPromise().then(this::logEvent);
        awaitEvents(2);

        assertEvents("event 1", "return value");
    }

    @Test
    public void handlers_can_return_later_satisfied_Promises() {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<ResultsInterface> actor = actorThread.bindActor(ResultsInterface.class, new ResultsAdapter() {
            Promise.Deferred<String> deferred;

            @Override
            public Promise<String> returnsPromise() {
                logEvent("event 1");
                deferred = Promise.defer();
                return deferred.promise();
            }

            @Override
            public void noReturnValue() {
                logEvent("event 2");
                deferred.resolve("return value");
            }
        });

        actor.tell().returnsPromise().then(this::logEvent);
        actor.tell().noReturnValue();
        awaitEvents(3);

        assertEvents("event 1", "event 2", "return value");
    }

    @Test
    public void handlers_can_return_any_Future_implementation() throws Exception {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<ResultsInterface> actor = actorThread.bindActor(ResultsInterface.class, new ResultsAdapter() {
            FutureTask<String> future;

            @Override
            public Future<String> returnsFuture() {
                logEvent("event 1");
                future = new FutureTask<>(() -> "return value");
                return future;
            }

            @Override
            public void noReturnValue() {
                future.run();
                logEvent("event 2");
            }
        });

        Future<String> future = actor.tell().returnsFuture();
        actor.tell().noReturnValue();
        awaitEvents(2);

        assertEvents("event 1", "event 2");
        assertThat(future.get(1, TimeUnit.MILLISECONDS), is("return value"));
    }

    // TODO: handlers_can_return_notification_promises (no return value)
    // TODO: on_failure_promise_is_cancelled (or pass the exception to callback? should error handler also receive it?)

    // threads

    @Test
    public void actors_bound_to_the_same_actor_thread_are_processed_in_the_same_thread() {
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


    // access to current actor thread

    @Test
    public void current_actor_thread_can_be_accessed_inside_an_actor() throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<ActorThread> getCurrentThread = new FutureTask<>(Actors::currentThread);

        ActorThread actorThread = actors.startActorThread();
        ActorRef<Runnable> actor = actorThread.bindActor(Runnable.class, getCurrentThread);
        actor.tell().run();
        processEvents();

        ActorThread currentThread = getCurrentThread.get(100, TimeUnit.MILLISECONDS);
        assertThat(currentThread, is(actorThread));
    }

    @Test
    public void current_actor_thread_cannot_be_accessed_outside_an_actor() {
        ActorThread actorThread = actors.startActorThread();
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new SpyDummyListener());
        actor.tell().onSomething("foo");
        awaitEvents(1);

        thrown.expect(IllegalStateException.class);
        Actors.currentThread();
    }


    // failure handling

    @Test
    public void exceptions_thrown_by_actors_are_given_to_the_FailureHandler() {
        SpyFailureHandler failureHandler = new SpyFailureHandler();
        DummyExceptionThrowingActor throwerActor = new DummyExceptionThrowingActor("dummy exception");
        ActorRef<DummyListener> actor = bindActorWithFailureHandler(failureHandler, throwerActor);

        actor.tell().onSomething("the message");
        awaitEvents(1);

        assertThat(failureHandler.lastActor, is((Object) throwerActor));
        assertThat(failureHandler.lastMessage, is((Object) new OnSomethingEvent("the message")));
        assertThat(failureHandler.lastException, is((Throwable) throwerActor.thrownException));
    }

    @Test
    public void with_a_graceful_failure_handler_the_actor_thread_will_continue_processing_messages() {
        FailureHandler gracefulFailureHandler = new FailureHandler() {
            @Override
            public void uncaughtException(Object actor, Object message, Throwable exception) {
                // just log the exception or stuff like that
            }
        };
        DummyListener throwOnFirstCall = new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                logEvent(parameter);
                if (parameter.equals("first")) {
                    throw new DummyException();
                }
            }
        };
        ActorRef<DummyListener> actor = bindActorWithFailureHandler(gracefulFailureHandler, throwOnFirstCall);

        actor.tell().onSomething("first");
        actor.tell().onSomething("second");
        awaitEvents(2);

        assertEvents("first", "second");
    }

    @Test
    public void with_an_interrupting_failure_handler_the_actor_thread_will_stop_processing_messages() {
        FailureHandler interruptingFailureHandler = new FailureHandler() {
            @Override
            public void uncaughtException(Object actor, Object message, Throwable exception) {
                // log the exception, then stop the actor thread by interrupting it
                Thread.currentThread().interrupt();
            }
        };
        DummyListener throwOnFirstCall = new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                logEvent(parameter);
                if (parameter.equals("first")) {
                    throw new DummyException();
                }
            }
        };
        ActorRef<DummyListener> actor = bindActorWithFailureHandler(interruptingFailureHandler, throwOnFirstCall);

        actor.tell().onSomething("first");
        actor.tell().onSomething("second");
        awaitEvents(1);
        expectNoMoreEvents();

        assertEvents("first");
    }

    private ActorRef<DummyListener> bindActorWithFailureHandler(FailureHandler failureHandler, DummyListener rawActor) {
        T actors = newActors(defaultEventizerProvider, failureHandler, defaultMessageListener);
        ActorThread actorThread = actors.startActorThread();
        return actorThread.bindActor(DummyListener.class, rawActor);
    }


    // message logging

    @Test
    public void sending_and_processing_messages_is_logged() {
        MessageListener messageListener = mock(MessageListener.class);
        actors = newActors(defaultEventizerProvider, defaultFailureHandler, messageListener);
        ActorThread actorThread = actors.startActorThread();
        DummyListener rawActor = mock(DummyListener.class);
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, rawActor);

        actor.tell().onSomething("parameter");
        sendSyncEvent(actorThread);                 // wait for onProcessingFinished to be called
        awaitEvents(1);

        InOrder inOrder = inOrder(messageListener);
        inOrder.verify(messageListener).onMessageSent(new OnSomethingEvent("parameter"));
        inOrder.verify(messageListener).onProcessingStarted(rawActor, new OnSomethingEvent("parameter"));
        inOrder.verify(messageListener).onProcessingFinished();
    }
}
