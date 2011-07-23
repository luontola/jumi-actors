// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import net.orfjackal.jumi.core.dynamicevents.DynamicListenerFactory;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MultiThreadedActorsTest extends ActorsContract<MultiThreadedActors> {

    private final List<MultiThreadedActors> createdActorses = new ArrayList<MultiThreadedActors>();

    protected MultiThreadedActors newActors(ListenerFactory<?>... factories) {
        MultiThreadedActors actors = new MultiThreadedActors(factories);
        createdActorses.add(actors);
        return actors;
    }

    protected void processEvents() {
        // noop; background threads run automatically, rely on the timeouts in the contract tests for waiting
    }

    @After
    public void shutdown() throws InterruptedException {
        for (MultiThreadedActors actors : createdActorses) {
            actors.shutdown(TIMEOUT);
        }
    }


    // normal event-polling actors

    @Test
    public void target_is_invoked_in_its_own_actor_thread() throws InterruptedException {
        SpyDummyListener actor = new SpyDummyListener();

        DummyListener handle = actors.startEventPoller(DummyListener.class, actor, "ActorName");
        handle.onSomething("event");
        awaitEvents(1);

        assertThat(actor.thread.getName(), is("ActorName"));
    }

    // unattended workers

    @Test
    public void unattended_workers_are_run_in_their_own_thread() throws InterruptedException {
        SpyRunnable worker = new SpyRunnable("event 2");
        SpyRunnable actor = new WorkerStartingSpyRunnable("event 1", worker, new NullRunnable());

        actors.startEventPoller(Runnable.class, actor, "ActorName").run();
        awaitEvents(2);

        assertThat("worker is run", worker.thread, is(notNullValue()));
        assertThat("worker is run in its own thread", worker.thread, is(not(Thread.currentThread())));
        assertThat("worker is run in its own thread", worker.thread, is(not(actor.thread)));
    }

    // shutdown

    @Test
    public void actor_threads_can_be_shut_down() throws InterruptedException {
        SpyDummyListener actor = new SpyDummyListener();
        DummyListener handle = actors.startEventPoller(DummyListener.class, actor, "ActorName");
        handle.onSomething("event");
        awaitEvents(1);

        assertThat("alive before shutdown", actor.thread.isAlive(), is(true));
        actors.shutdown(TIMEOUT);

        assertThat("alive after shutdown", actor.thread.isAlive(), is(false));
    }

    @Test
    public void shutting_down_waits_for_workers_to_finish() throws InterruptedException {
        actors = newActors(new DummyListenerFactory(), new DynamicListenerFactory<Runnable>(Runnable.class));
        final BlockingQueue<String> events = new LinkedBlockingQueue<String>();

        final Runnable worker = new Runnable() {
            public void run() {
                events.add("worker started");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // continue executing
                }
                events.add("worker finished");
            }
        };
        actors.startEventPoller(Runnable.class, new WorkerStartingSpyRunnable("", worker, new NullRunnable()), "Actor").run();

        assertThat("worker did not start", events.poll(TIMEOUT, TimeUnit.MILLISECONDS), is("worker started"));
        actors.shutdown(TIMEOUT);
        assertThat("did not wait for worker to finish", events.poll(), is("worker finished"));
    }
}
