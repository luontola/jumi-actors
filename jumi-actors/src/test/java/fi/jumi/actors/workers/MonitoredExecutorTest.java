// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import fi.jumi.actors.*;
import org.junit.*;

import java.util.concurrent.*;

public class MonitoredExecutorTest {

    private static final long TIMEOUT = 1000;

    private final EventSpy events = new EventSpy();
    private final UncaughtExceptionCollector uncaughtExceptions = new UncaughtExceptionCollector();
    private ExecutorService realExecutor;

    private final ActorRef<Runnable> callback = ActorRef.<Runnable>wrap(new Runnable() {
        @Override
        public void run() {
            events.log("callback");
        }
    });
    private final WorkerCounter workerCounter = new WorkerCounter(callback);


    @Before
    public void startExecutor() {
        realExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(uncaughtExceptions);
                return t;
            }
        });
    }

    @After
    public void stopExecutor() throws Throwable {
        realExecutor.shutdown();
        uncaughtExceptions.failIfNotEmpty();
    }


    @Test(timeout = TIMEOUT)
    public void the_callback_is_fired_after_all_commands_are_finished() throws InterruptedException {
        Executor executor = new MonitoredExecutor(realExecutor, workerCounter);

        executor.execute(new Command("command"));

        events.await(2, TIMEOUT);
        events.assertContains("command", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void works_for_concurrent_commands() throws InterruptedException {
        Executor executor = new MonitoredExecutor(realExecutor, workerCounter);

        CyclicBarrier barrier = new CyclicBarrier(2);
        executor.execute(new SyncedCommand1(barrier));
        executor.execute(new SyncedCommand2(barrier));

        events.await(3, TIMEOUT);
        events.assertContains("command 1", "command 2", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void works_for_commands_which_launch_other_commands() throws InterruptedException {
        final Executor executor = new MonitoredExecutor(realExecutor, workerCounter);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                events.log("command 1");
                executor.execute(new Command("command 2"));
            }
        });

        events.await(3, TIMEOUT);
        events.assertContains("command 1", "command 2", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void multiple_executors_can_share_the_same_callback() throws InterruptedException {
        Executor executor1 = new MonitoredExecutor(realExecutor, workerCounter);
        Executor executor2 = new MonitoredExecutor(realExecutor, workerCounter);

        CyclicBarrier barrier = new CyclicBarrier(2);
        executor1.execute(new SyncedCommand1(barrier));
        executor2.execute(new SyncedCommand2(barrier));

        events.await(3, TIMEOUT);
        events.assertContains("command 1", "command 2", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void the_callback_is_called_even_when_commands_throw_exceptions() throws InterruptedException {
        Executor executor = new MonitoredExecutor(realExecutor, workerCounter);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                events.log("thrower");
                throw uncaughtExceptions.throwDummyException();
            }
        });

        events.await(2, TIMEOUT);
        events.assertContains("thrower", "callback");
    }


    // helpers

    private void await(CyclicBarrier barrier) {
        try {
            barrier.await(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class Command implements Runnable {
        private final String command;

        private Command(String command) {
            this.command = command;
        }

        @Override
        public void run() {
            events.log(command);
        }
    }

    private class SyncedCommand1 implements Runnable {
        private final CyclicBarrier barrier;

        public SyncedCommand1(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            events.log("command 1");
            await(barrier);
        }
    }

    private class SyncedCommand2 implements Runnable {
        private final CyclicBarrier barrier;

        public SyncedCommand2(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            await(barrier);
            events.log("command 2");
        }
    }
}
