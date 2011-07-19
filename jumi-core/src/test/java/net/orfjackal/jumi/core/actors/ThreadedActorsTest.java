// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import net.orfjackal.jumi.core.dynamicevents.DynamicListenerFactory;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ThreadedActorsTest extends ActorsContract {

    private static final long TIMEOUT = 1000;

    private final List<ThreadedActors> createdActors = new ArrayList<ThreadedActors>();
    private ThreadedActors actors;

    protected ThreadedActors newActors(ListenerFactory<?>... factories) {
        ThreadedActors actors = new ThreadedActors(factories);
        createdActors.add(actors);
        return actors;
    }

    protected void processEvents() {
        // noop; background threads run automatically, rely on the timeouts in the contract tests for waiting
    }

    @Before
    public void initThreadedActors() {
        actors = newActors(new DummyListenerFactory(), new DynamicListenerFactory<Runnable>(Runnable.class));
    }

    @After
    public void shutdown() throws InterruptedException {
        for (ThreadedActors actors : createdActors) {
            actors.shutdown(TIMEOUT);
        }
    }


    // normal event-polling actors

    @Test
    public void target_is_invoked_in_its_own_actor_thread() throws InterruptedException {
        LinkedBlockingQueue<Thread> spy = new LinkedBlockingQueue<Thread>();
        DummyListener handle = actors.startEventPoller(DummyListener.class, new CurrentThreadSpy(spy), "ActorName");

        handle.onSomething(null);

        Thread thread = spy.poll(TIMEOUT, TimeUnit.MILLISECONDS);
        assertThat(thread.getName(), is("ActorName"));
    }

    // unattended workers

    @Test
    public void unattended_workers_are_run_in_their_own_thread() throws InterruptedException {
        final AtomicReference<Thread> actorThread = new AtomicReference<Thread>();
        final AtomicReference<Thread> workerThread = new AtomicReference<Thread>();
        final CountDownLatch done = new CountDownLatch(1);

        final Runnable worker = new Runnable() {
            public void run() {
                workerThread.set(Thread.currentThread());
                done.countDown();
            }
        };
        actors.startEventPoller(Runnable.class, new Runnable() {
            public void run() {
                actorThread.set(Thread.currentThread());
                actors.startUnattendedWorker(worker, new DummyRunnable());
            }
        }, "Actor").run();
        done.await(TIMEOUT, TimeUnit.MILLISECONDS);

        assertThat("worker was not run", workerThread.get(), is(notNullValue()));
        assertThat("worker did not have its own thread", workerThread.get(), is(not(Thread.currentThread())));
        assertThat("worker did not have its own thread", workerThread.get(), is(not(actorThread.get())));
    }

    // shutdown

    @Test
    public void actor_threads_can_be_shut_down() throws InterruptedException {
        LinkedBlockingQueue<Thread> spy = new LinkedBlockingQueue<Thread>();
        DummyListener handle = actors.startEventPoller(DummyListener.class, new CurrentThreadSpy(spy), "ActorName");
        handle.onSomething(null);
        Thread actorThread = spy.poll(TIMEOUT, TimeUnit.MILLISECONDS);

        assertThat("alive before shutdown", actorThread.isAlive(), is(true));
        actors.shutdown(TIMEOUT);
        assertThat("alive after shutdown", actorThread.isAlive(), is(false));
    }

    @Test
    public void shutting_down_waits_for_workers_to_finish() throws InterruptedException {
        final ThreadedActors actors = newActors(new DummyListenerFactory(), new DynamicListenerFactory<Runnable>(Runnable.class));
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
        actors.startEventPoller(Runnable.class, new Runnable() {
            public void run() {
                actors.startUnattendedWorker(worker, new DummyRunnable());
            }
        }, "Actor").run();

        assertThat("worker did not start", events.poll(TIMEOUT, TimeUnit.MILLISECONDS), is("worker started"));
        actors.shutdown(TIMEOUT);
        assertThat("did not wait for worker to finish", events.poll(), is("worker finished"));
    }
}
