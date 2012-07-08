// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import fi.jumi.actors.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class WorkerCountingExecutorTest {

    private static final long TIMEOUT = 1000;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final EventSpy events = new EventSpy();
    private final UncaughtExceptionCollector uncaughtExceptions = new UncaughtExceptionCollector();
    private ExecutorService realExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        private int counter = 0;

        @Override
        public Thread newThread(Runnable r) {
            counter++;
            Thread t = new Thread(r, "pool-thread-" + counter);
            t.setUncaughtExceptionHandler(uncaughtExceptions);
            return t;
        }
    });

    private final ActorRef<WorkerListener> callback = ActorRef.<WorkerListener>wrap(new WorkerListener() {
        @Override
        public void onAllWorkersFinished() {
            events.log("callback");
        }
    });

    @After
    public void stopExecutor() throws Throwable {
        realExecutor.shutdownNow();
        assertTrue(realExecutor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS));
        uncaughtExceptions.failIfNotEmpty();
    }


    @Test(timeout = TIMEOUT)
    public void the_callback_is_fired_after_all_commands_are_finished() throws InterruptedException {
        WorkerCounter counter = new WorkerCounter(realExecutor, callback);

        counter.execute(new Command("command"));
        counter.startInitialWorkers();

        events.await(2, TIMEOUT);
        events.assertContains("command", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void works_for_concurrent_commands() throws InterruptedException {
        WorkerCounter counter = new WorkerCounter(realExecutor, callback);

        CyclicBarrier barrier = new CyclicBarrier(2);
        counter.execute(new SyncedCommand1(barrier));
        counter.execute(new SyncedCommand2(barrier));
        counter.startInitialWorkers();

        events.await(3, TIMEOUT);
        events.assertContains("command 1", "command 2", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void works_for_commands_which_launch_other_commands() throws InterruptedException {
        final WorkerCounter counter = new WorkerCounter(realExecutor, callback);

        counter.execute(new Runnable() {
            @Override
            public void run() {
                events.log("command 1");
                counter.execute(new Command("command 2"));
            }
        });
        counter.startInitialWorkers();

        events.await(3, TIMEOUT);
        events.assertContains("command 1", "command 2", "callback");
    }

    /**
     * Reproduces a concurrency bug that would produce two onFinished events
     * if the first command finishes before the second command is even scheduled.
     */
    @Test(timeout = TIMEOUT)
    public void works_for_synchronous_executors() throws InterruptedException {
        Executor synchronousExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
        WorkerCounter counter = new WorkerCounter(synchronousExecutor, callback);

        counter.execute(new Command("command 1"));
        counter.execute(new Command("command 2"));
        counter.startInitialWorkers();

        events.await(3, TIMEOUT);
        events.assertContains("command 1", "command 2", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void the_callback_is_called_even_when_commands_throw_exceptions() throws InterruptedException {
        WorkerCounter counter = new WorkerCounter(realExecutor, callback);
        Runnable throwerCommand = new Runnable() {
            @Override
            public void run() {
                events.log("thrower");
                throw uncaughtExceptions.throwDummyException();
            }
        };

        counter.execute(throwerCommand);
        counter.startInitialWorkers();

        events.await(2, TIMEOUT);
        events.assertContains("thrower", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void startInitialWorkers_cannot_be_called_twice() {
        WorkerCounter counter = new WorkerCounter(realExecutor, callback);
        counter.startInitialWorkers();

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("initial workers have already been started");
        counter.startInitialWorkers();
    }

    @Test(timeout = TIMEOUT)
    public void toString_of_commands_is_good_for_logging() {
        final StringBuilder loggerOutput = new StringBuilder();
        Executor loggingExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                loggerOutput.append(command.toString());
            }
        };
        WorkerCounter counter = new WorkerCounter(loggingExecutor, callback);
        Runnable originalCommand = mock(Runnable.class, "<the command's original toString>");

        counter.execute(originalCommand);
        counter.startInitialWorkers();

        assertThat(loggerOutput.toString(), containsString(originalCommand.toString()));
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
