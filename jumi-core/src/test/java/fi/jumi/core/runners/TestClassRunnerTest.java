// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.Startable;
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
            new TestClassListenerFactory()
    );

    @Test
    public void test_class_with_zero_tests() {
        // TODO: is this an allowed situation? in practice it means that the class is not reported anywhere
        listener.onTestClassFinished();

        runAndAwaitCompletion(new TestClassRunner(DummyTest.class, ZeroTestsDriver.class, listener, actors));
    }

    @Test
    public void test_class_with_only_root_test() {
        listener.onTestFound(TestId.ROOT, "root test");
        listener.onTestClassFinished();

        runAndAwaitCompletion(new TestClassRunner(DummyTest.class, OneTestDriver.class, listener, actors));
    }

    @Test
    public void test_class_with_one_failing_test() {
        listener.onTestFound(TestId.ROOT, "root test");
        listener.onTestStarted(TestId.ROOT);
        listener.onFailure(TestId.ROOT, new Exception("dummy failure"));
        listener.onTestFinished(TestId.ROOT);
        listener.onTestClassFinished();

        runAndAwaitCompletion(new TestClassRunner(DummyTest.class, OneFailingTestDriver.class, listener, actors));
    }

    // TODO: how to distinguish between events from concurrent executions of the same test?

    private void runAndAwaitCompletion(TestClassRunner runner) {
        spy.replay();
        actors.createPrimaryActor(Startable.class, runner, "TestClassRunner").start();
        actors.processEventsUntilIdle();
        spy.verify();
    }


    private static class DummyTest {
    }

    static class ZeroTestsDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }

    static class OneTestDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "root test");
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
}
