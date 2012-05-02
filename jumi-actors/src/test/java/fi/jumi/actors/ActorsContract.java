// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.eventizers.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class ActorsContract<T extends Actors> extends ActorsContractHelpers<T> {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void initActors() {
        actors = newActors(new ComposedEventizerProvider(new DummyListenerEventizer()));
    }

    protected abstract T newActors(EventizerProvider eventizerProvider);


    // actors

    @Test
    public void method_calls_on_ActorRef_are_forwarded_to_the_actor() throws InterruptedException {
        ActorThread actorThread = actors.startActorThread("ActorThread");
        ActorRef<DummyListener> actorRef = actorThread.bindActor(DummyListener.class, new SpyDummyListener());

        actorRef.tell().onSomething("event parameter");
        awaitEvents(1);

        assertEvents("event parameter");
    }

    @Test
    public void events_to_an_actor_are_processed_in_the_order_they_were_sent() throws InterruptedException {
        ActorThread actorThread = actors.startActorThread("ActorThread");
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new SpyDummyListener());

        actor.tell().onSomething("event 1");
        actor.tell().onSomething("event 2");
        actor.tell().onSomething("event 3");
        awaitEvents(3);

        assertEvents("event 1", "event 2", "event 3");
    }

    /**
     * This is to prevent resource leaks, because each event poller uses one thread, and as of writing
     * the system does not try to release the threads.
     */
    @Test
    public void actor_threads_cannot_be_started_inside_other_actor_threads() {
        final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();

        ActorThread actorThread = actors.startActorThread("ActorThread 1");
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                try {
                    actors.startActorThread("ActorThread 2");
                } catch (Throwable t) {
                    thrown.set(t);
                } finally {
                    logEvent("test finished");
                }
            }
        });
        actor.tell().onSomething("");

        awaitEvents(1);

        Throwable t = thrown.get();
        assertThat(t, is(instanceOf(IllegalStateException.class)));
        assertThat(t.getMessage(), containsString("already inside an actor thread"));
    }

    @Test
    public void actors_bound_to_the_same_actor_thread_are_processed_in_the_same_thread() {
        actors = newActors(new DynamicEventizerProvider());
        ActorThread actorThread = actors.startActorThread("ActorName");

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


    // setup

    @Test
    public void eventizers_must_be_registered_for_them_to_be_usable() {
        ActorThread actorThread = actors.startActorThread("ActorThread");
        NoEventizerForThisListener listener = new NoEventizerForThisListener() {
        };

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported type");
        thrown.expectMessage(NoEventizerForThisListener.class.getName());

        actorThread.bindActor(NoEventizerForThisListener.class, listener);
    }
}
