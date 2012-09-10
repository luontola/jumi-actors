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
import static org.mockito.Mockito.mock;

public class WorkerCounterTest {

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

    @After
    public void stopExecutor() throws Throwable {
        realExecutor.shutdownNow();
        uncaughtExceptions.failIfNotEmpty();
    }


    @Test(timeout = TIMEOUT)
    public void the_callback_is_fired_after_all_commands_are_finished() throws InterruptedException {
        WorkerCounter counter = new WorkerCounter(realExecutor);

        counter.execute(new Command("command"));
        counter.afterPreviousWorkersFinished(log("callback"));

        events.await(2, TIMEOUT);
        events.assertContains("command", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void the_callback_is_fired_immediately_if_there_were_no_commands() throws InterruptedException {
        WorkerCounter counter = new WorkerCounter(realExecutor);

        counter.afterPreviousWorkersFinished(log("callback"));

        events.await(1, TIMEOUT);
        events.assertContains("callback");
    }

    @Test(timeout = TIMEOUT)
    public void a_new_callback_can_be_set_after_workers_are_finished() {
        WorkerCounter counter = new WorkerCounter(realExecutor);
        counter.afterPreviousWorkersFinished(log("callback 1"));

        counter.afterPreviousWorkersFinished(log("callback 2"));

        events.assertContains("callback 1", "callback 2");
    }

    @Test(timeout = TIMEOUT)
    public void a_new_callback_cannot_be_set_before_workers_are_finished() {
        WorkerCounter counter = new WorkerCounter(realExecutor);

        CyclicBarrier barrier = new CyclicBarrier(2);
        counter.execute(new SyncedCommand1(barrier)); // will not finish before the end of this test
        counter.afterPreviousWorkersFinished(log("callback 1"));

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("a callback already exists");
        counter.afterPreviousWorkersFinished(log("callback 2"));
    }

    @Test(timeout = TIMEOUT)
    public void works_for_concurrent_commands() throws InterruptedException {
        WorkerCounter counter = new WorkerCounter(realExecutor);

        CyclicBarrier barrier = new CyclicBarrier(2);
        counter.execute(new SyncedCommand1(barrier));
        counter.execute(new SyncedCommand2(barrier));
        counter.afterPreviousWorkersFinished(log("callback"));

        events.await(3, TIMEOUT);
        events.assertContains("command 1", "command 2", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void works_for_commands_which_launch_other_commands() throws InterruptedException {
        final WorkerCounter counter = new WorkerCounter(realExecutor);

        counter.execute(new Runnable() {
            @Override
            public void run() {
                events.log("command 1");
                counter.execute(new Command("command 2"));
            }
        });
        counter.afterPreviousWorkersFinished(log("callback"));

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
        WorkerCounter counter = new WorkerCounter(synchronousExecutor);

        counter.execute(new Command("command 1"));
        counter.execute(new Command("command 2"));
        counter.afterPreviousWorkersFinished(log("callback"));

        events.await(3, TIMEOUT);
        events.assertContains("command 1", "command 2", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void the_callback_is_called_even_when_commands_throw_exceptions() throws InterruptedException {
        WorkerCounter counter = new WorkerCounter(realExecutor);
        Runnable throwerCommand = new Runnable() {
            @Override
            public void run() {
                events.log("thrower");
                throw uncaughtExceptions.throwDummyException();
            }
        };

        counter.execute(throwerCommand);
        counter.afterPreviousWorkersFinished(log("callback"));

        events.await(2, TIMEOUT);
        events.assertContains("thrower", "callback");
    }

    @Test(timeout = TIMEOUT)
    public void toString_of_commands_is_good_for_logging() {
        final StringBuilder loggerOutput = new StringBuilder();
        Executor loggingExecutor = new Executor() {
            @Override
            public void execute(Runnable wrappedCommand) {
                loggerOutput.append(wrappedCommand.toString());
            }
        };
        WorkerCounter counter = new WorkerCounter(loggingExecutor);
        Runnable originalCommand = mock(Runnable.class, "<the command's original toString>");

        counter.execute(originalCommand);

        assertThat(loggerOutput.toString(), containsString(originalCommand.toString()));
    }


    // helpers

    private ActorRef<WorkerListener> log(final String message) {
        return ActorRef.<WorkerListener>wrap(new WorkerListener() {
            @Override
            public void onAllWorkersFinished() {
                events.log(message);
            }
        });
    }

    private void await(CyclicBarrier barrier) {
        try {
            barrier.await(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
