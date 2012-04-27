// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.dynamicevents.DynamicEventizer;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MultiThreadedActorsTest extends ActorsContract<MultiThreadedActors> {

    private final List<MultiThreadedActors> createdActorses = new ArrayList<MultiThreadedActors>();

    @Override
    protected MultiThreadedActors newActors(Eventizer<?>... factories) {
        class SilentUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                // do not print failures; it keeps the test logs cleaner
            }
        }
        ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(new SilentUncaughtExceptionHandler());
                return t;
            }
        });
        MultiThreadedActors actors = new MultiThreadedActors(threadPool, factories);
        createdActorses.add(actors);
        return actors;
    }

    @Override
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
        SpyDummyListener rawActor = new SpyDummyListener();

        ActorThread actorThread = actors.startActorThread("ActorName");
        ActorRef<DummyListener> actorRef = actorThread.bindActor(DummyListener.class, rawActor);
        actorRef.tell().onSomething("event");
        awaitEvents(1);

        assertThat(rawActor.thread.getName(), is("ActorName"));
    }

    // unattended workers

    @Test
    public void unattended_workers_are_run_in_their_own_thread() throws InterruptedException {
        SpyRunnable worker = new SpyRunnable("event 2");
        SpyRunnable rawActor = new WorkerStartingSpyRunnable("event 1", worker, new NullRunnable());

        ActorThread actorThread = actors.startActorThread("ActorName");
        ActorRef<Runnable> actorRef = actorThread.bindActor(Runnable.class, rawActor);
        actorRef.tell().run();
        awaitEvents(2);

        assertThat("worker is run", worker.thread, is(notNullValue()));
        assertThat("worker is run in its own thread", worker.thread, is(not(Thread.currentThread())));
        assertThat("worker is run in its own thread", worker.thread, is(not(rawActor.thread)));
    }

    // shutdown

    @Test
    public void actor_threads_can_be_shut_down() throws InterruptedException {
        SpyDummyListener rawActor = new SpyDummyListener();
        ActorThread actorThread = actors.startActorThread("ActorName");
        ActorRef<DummyListener> actorRef = actorThread.bindActor(DummyListener.class, rawActor);
        actorRef.tell().onSomething("event");
        awaitEvents(1);

        assertThat("alive before shutdown", rawActor.thread.isAlive(), is(true));
        actors.shutdown(TIMEOUT);

        assertThat("alive after shutdown", rawActor.thread.isAlive(), is(false));
    }

    @Test
    public void shutting_down_waits_for_workers_to_finish() throws InterruptedException {
        actors = newActors(new DummyListenerEventizer(), new DynamicEventizer<Runnable>(Runnable.class));
        final BlockingQueue<String> events = new LinkedBlockingQueue<String>();

        final Runnable worker = new Runnable() {
            @Override
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
        ActorThread actorThread = actors.startActorThread("Actor");
        ActorRef<Runnable> actor = actorThread.bindActor(Runnable.class, new WorkerStartingSpyRunnable("", worker, new NullRunnable()));
        actor.tell().run();

        assertThat("worker did not start", events.poll(TIMEOUT, TimeUnit.MILLISECONDS), is("worker started"));
        actors.shutdown(TIMEOUT);
        assertThat("did not wait for worker to finish", events.poll(), is("worker finished"));
    }
}
