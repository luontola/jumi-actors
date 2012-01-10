// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.Startable;
import fi.jumi.core.events.executor.*;
import fi.jumi.core.events.runnable.RunnableFactory;
import fi.jumi.core.events.startable.StartableFactory;
import fi.jumi.core.events.testclass.TestClassListenerFactory;
import org.junit.Test;

import java.util.concurrent.Executor;

public class TestClassRunnerTest {

    private final SpyListener<TestClassRunnerListener> spy = new SpyListener<TestClassRunnerListener>(TestClassRunnerListener.class);
    private final TestClassRunnerListener listener = spy.getListener();
    private final SingleThreadedActors actors = new SingleThreadedActors(
            new StartableFactory(),
            new RunnableFactory(),
            new ExecutorFactory(),
            new ExecutorListenerFactory(),
            new TestClassListenerFactory()
    );

    @Test
    public void test_class_with_zero_tests() {
        // TODO: is this an allowed situation? in practice it means that the class is not reported anywhere
        listener.onTestClassFinished();

        runAndAwaitCompletion(ZeroTestsDriver.class);
    }

    @Test
    public void test_class_with_one_passing_tests() {
        listener.onTestFound(TestId.ROOT, "root test");
        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        listener.onTestClassFinished();

        runAndAwaitCompletion(OnePassingTestDriver.class);
    }

    @Test
    public void test_class_with_one_failing_test() {
        listener.onTestFound(TestId.ROOT, "root test");
        listener.onTestStarted(TestId.ROOT);
        listener.onFailure(TestId.ROOT, new Exception("dummy failure"));
        listener.onTestFinished(TestId.ROOT);
        listener.onTestClassFinished();

        runAndAwaitCompletion(OneFailingTestDriver.class);
    }

    @Test
    public void test_class_with_multiple_tests_which_are_run_in_parallel() {
        listener.onTestFound(TestId.ROOT, "root test");
        listener.onTestFound(TestId.of(0), "test one");
        listener.onTestFound(TestId.of(1), "test two");
        listener.onTestStarted(TestId.of(0));
        listener.onTestStarted(TestId.of(1));
        listener.onTestFinished(TestId.of(0));
        listener.onTestFinished(TestId.of(1));
        listener.onTestClassFinished();

        runAndAwaitCompletion(ManyTestsInParallelDriver.class);
    }

    @Test
    public void the_executor_can_be_used_to_run_tests() {
        listener.onTestFound(TestId.ROOT, "root test");
        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        listener.onTestClassFinished();

        runAndAwaitCompletion(UseExecutorDriver.class);
    }


    // helpers

    private void runAndAwaitCompletion(Class<? extends Driver> driverClass) {
        runAndAwaitCompletion(new TestClassRunner(DummyTest.class, driverClass, listener, actors, actors.getExecutor()));
    }

    private void runAndAwaitCompletion(TestClassRunner runner) {
        spy.replay();
        actors.createPrimaryActor(Startable.class, runner, "TestClassRunner").start();
        actors.processEventsUntilIdle();
        spy.verify();
    }


    // guinea pigs

    private static class DummyTest {
    }

    static class ZeroTestsDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }

    static class OnePassingTestDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "root test");
            TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
            tn.fireTestFinished();
        }
    }

    static class OneFailingTestDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "root test");
            TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
            tn.fireFailure(new Exception("dummy failure"));
            tn.fireTestFinished();
        }
    }

    static class ManyTestsInParallelDriver implements Driver {
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "root test");
            notifier.fireTestFound(TestId.of(0), "test one");
            notifier.fireTestFound(TestId.of(1), "test two");

            TestNotifier tn1 = notifier.fireTestStarted(TestId.of(0));
            TestNotifier tn2 = notifier.fireTestStarted(TestId.of(1));
            tn1.fireTestFinished();
            tn2.fireTestFinished();
        }
    }

    static class UseExecutorDriver implements Driver {
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "root test");
            executor.execute(new Runnable() {
                public void run() {
                    TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
                    tn.fireTestFinished();
                }
            });
        }
    }
}
