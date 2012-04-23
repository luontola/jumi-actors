// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.dynamicevents.DynamicEventizer;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public abstract class ActorsContract<T extends Actors> extends ActorsContractHelpers<T> {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void initActors() {
        actors = newActors(new DummyListenerEventizer(), new DynamicEventizer<Runnable>(Runnable.class));
    }

    protected abstract T newActors(Eventizer<?>... factories);


    // normal event-polling actors

    @Test
    public void method_calls_on_handle_are_forwarded_to_target() throws InterruptedException {
        ActorRef<DummyListener> actor = actors.createPrimaryActor(DummyListener.class, new SpyDummyListener(), "ActorName");

        actor.tell().onSomething("event parameter");
        awaitEvents(1);

        assertEvents("event parameter");
    }

    @Test
    public void actor_processes_multiple_events_in_the_order_they_were_sent() throws InterruptedException {
        ActorRef<DummyListener> actor = actors.createPrimaryActor(DummyListener.class, new SpyDummyListener(), "ActorName");

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
    public void event_pollers_cannot_be_started_inside_other_event_pollers() {
        final AtomicReference<Throwable> thrown = new AtomicReference<Throwable>();

        actors.createPrimaryActor(DummyListener.class, new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                try {
                    actors.createPrimaryActor(DummyListener.class, new SpyDummyListener(), "Actor 2");
                } catch (Throwable t) {
                    thrown.set(t);
                } finally {
                    logEvent("done");
                }
            }
        }, "Actor 1").tell().onSomething("");

        awaitEvents(1);

        Throwable t = thrown.get();
        assertThat(t, is(instanceOf(IllegalStateException.class)));
        assertThat(t.getMessage(), containsString("already inside an actor"));
    }


    // secondary interfaces

    @Test
    public void an_actor_can_receive_events_in_the_same_thread_through_a_secondary_interface() {
        actors = newActors(DynamicEventizer.factoriesFor(PrimaryInterface.class, SecondaryInterface.class));
        class MultiPurposeActor implements PrimaryInterface, SecondaryInterface {
            public volatile ActorRef<SecondaryInterface> secondary;
            public volatile Thread primaryEventThread;
            public volatile Thread secondaryEventThread;

            @Override
            public void onPrimaryEvent() {
                // binding must be done inside an actor
                secondary = actors.createSecondaryActor(SecondaryInterface.class, this);

                primaryEventThread = Thread.currentThread();
                logEvent("primary event");
            }

            @Override
            public void onSecondaryEvent() {
                secondaryEventThread = Thread.currentThread();
                logEvent("secondary event");
            }
        }
        MultiPurposeActor actor = new MultiPurposeActor();

        ActorRef<PrimaryInterface> primary = actors.createPrimaryActor(PrimaryInterface.class, actor, "ActorName");
        primary.tell().onPrimaryEvent();
        awaitEvents(1);

        ActorRef<SecondaryInterface> secondary = actor.secondary;
        secondary.tell().onSecondaryEvent();
        awaitEvents(2);

        assertEvents("primary event", "secondary event");
        assertThat("secondary event happened", actor.secondaryEventThread, is(notNullValue()));
        assertThat("secondary event happened in same thread as primary event",
                actor.secondaryEventThread, is(actor.primaryEventThread));
    }


    @Test
    public void secondary_interfaces_cannot_be_bound_outside_an_actor() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("not inside an actor");

        actors.createSecondaryActor(DummyListener.class, mock(DummyListener.class));
    }


    // unattended workers

    @Test
    public void when_worker_finishes_the_actor_which_started_it_is_notified_in_the_actor_thread() throws InterruptedException {
        SpyRunnable worker = new SpyRunnable("run worker");
        SpyRunnable onFinished = new SpyRunnable("on finished");
        SpyRunnable actor = new WorkerStartingSpyRunnable("start worker", worker, onFinished);

        actors.createPrimaryActor(Runnable.class, actor, "Actor").tell().run();
        awaitEvents(3);

        assertEvents("start worker", "run worker", "on finished");
        assertThat("notification should have been in the actor thread", onFinished.thread, is(actor.thread));
    }

    @Test
    public void the_actor_is_notified_on_finish_even_if_the_worker_throws_an_exception() throws InterruptedException {
        // TODO: create a custom exception handler, then make it ignore this exception
        SpyRunnable worker = new ExceptionThrowingSpyRunnable("run worker", new RuntimeException("dummy exception"));
        SpyRunnable onFinished = new SpyRunnable("on finished");
        SpyRunnable actor = new WorkerStartingSpyRunnable("start worker", worker, onFinished);

        actors.createPrimaryActor(Runnable.class, actor, "Actor").tell().run();
        awaitEvents(3);

        assertEvents("start worker", "run worker", "on finished");
    }

    @Test
    public void unattended_workers_cannot_be_bound_outside_an_actor() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("not inside an actor");

        actors.startUnattendedWorker(new NullRunnable(), new NullRunnable());
    }


    // setup

    @Test
    public void listener_factories_must_be_registered_for_them_to_be_usable() {
        NoFactoryForThisListener listener = new NoFactoryForThisListener() {
        };

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported listener type");
        thrown.expectMessage(NoFactoryForThisListener.class.getName());

        actors.createPrimaryActor(NoFactoryForThisListener.class, listener, "ActorName");
    }
}
