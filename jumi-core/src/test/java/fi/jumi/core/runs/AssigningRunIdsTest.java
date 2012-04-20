// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;


import fi.jumi.api.drivers.*;
import fi.jumi.core.runners.TestClassListener;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;

public class AssigningRunIdsTest {

    private static final RunId FIRST_RUN_ID = new RunId(RunId.FIRST_ID);
    private static final RunId ANOTHER_RUN_ID = new RunId(RunId.FIRST_ID + 1);

    private final TestClassListener listener = mock(TestClassListener.class);
    private final RunIdSequence runIdSequence = new RunIdSequence();
    private final SuiteNotifier notifier = new DefaultSuiteNotifier(listener, runIdSequence);

    @Test
    public void RunId_is_assigned_when_a_test_is_started() {
        notifier.fireTestStarted(TestId.ROOT);

        verify(listener).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
    }

    @Test
    public void nested_tests_get_the_same_RunId() {
        notifier.fireTestStarted(TestId.ROOT);

        notifier.fireTestStarted(TestId.of(0));

        verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(0));
    }

    @Test
    public void siblings_of_nested_tests_get_the_same_RunId() {
        notifier.fireTestStarted(TestId.ROOT);
        notifier.fireTestStarted(TestId.of(0))
                .fireTestFinished();

        notifier.fireTestStarted(TestId.of(1));

        verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(1));
    }

    @Test
    public void nested_tests_in_child_threads_get_the_same_RunId() throws Exception {
        notifier.fireTestStarted(TestId.ROOT);

        execute(new Runnable() {
            @Override
            public void run() {
                notifier.fireTestStarted(TestId.of(0));
            }
        });

        verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(0));
    }

    @Test
    public void new_runs_get_a_different_RunId() {
        notifier.fireTestStarted(TestId.ROOT)
                .fireTestFinished();

        notifier.fireTestStarted(TestId.of(0));

        verify(listener).onTestStarted(ANOTHER_RUN_ID, TestId.of(0));
    }

    @Test
    public void concurrent_runs_in_other_threads_get_a_different_RunId() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(2);

        Runnable test1 = new Runnable() {
            @Override
            public void run() {
                TestNotifier tn1 = notifier.fireTestStarted(TestId.of(1));
                syncOn(barrier); // test 1 is be started first
                syncOn(barrier); // test 1 will be running when test 2 starts
                tn1.fireTestFinished();
            }
        };
        Runnable test2 = new Runnable() {
            @Override
            public void run() {
                syncOn(barrier);
                TestNotifier tn2 = notifier.fireTestStarted(TestId.of(2));
                syncOn(barrier);
                tn2.fireTestFinished();
            }
        };

        execute(test1, test2);

        verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(1));
        verify(listener).onTestStarted(ANOTHER_RUN_ID, TestId.of(2));
    }

    private void syncOn(CyclicBarrier sync) {
        try {
            sync.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    private static void execute(Runnable... tasks) throws Exception {
        List<Future<?>> futures = new ArrayList<Future<?>>();
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            for (Runnable task : tasks) {
                futures.add(executor.submit(task));
            }
            for (Future<?> future : futures) {
                future.get(1000, TimeUnit.MILLISECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
