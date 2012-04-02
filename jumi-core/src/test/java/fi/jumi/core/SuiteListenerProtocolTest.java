// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.events.executor.*;
import fi.jumi.core.events.runnable.RunnableFactory;
import fi.jumi.core.events.startable.StartableFactory;
import fi.jumi.core.events.testclass.TestClassListenerFactory;
import fi.jumi.core.events.testclassfinder.TestClassFinderListenerFactory;
import fi.jumi.core.files.*;
import fi.jumi.core.runners.*;
import org.junit.Test;

import java.util.concurrent.Executor;

public class SuiteListenerProtocolTest {

    private static final RunId RUN_1 = new RunId(42);

    private final SpyListener<SuiteListener> spy = new SpyListener<SuiteListener>(SuiteListener.class);
    private final SuiteListener listener = spy.getListener();

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new StartableFactory(),
            new RunnableFactory(),
            new ExecutorFactory(),
            new ExecutorListenerFactory(),
            new TestClassFinderListenerFactory(),
            new TestClassListenerFactory()
    );
    private final Executor executor = new SynchronousExecutor();

    @Test
    public void suite_with_zero_test_classes() {
        listener.onSuiteStarted();
        listener.onSuiteFinished();

        runAndCheckExpectations(null);
    }

    @Test
    public void suite_with_one_test_class_with_zero_tests() {
        listener.onSuiteStarted();
        listener.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        listener.onSuiteFinished();

        runAndCheckExpectations(ZeroTestsDriver.class, DummyTest.class);
    }

    @Test
    public void suite_with_one_test_class_with_tests() {
        listener.onSuiteStarted();
        listener.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        listener.onTestStarted(RUN_1, DummyTest.class.getName(), TestId.ROOT);
        listener.onTestFinished(RUN_1, DummyTest.class.getName(), TestId.ROOT);
        listener.onSuiteFinished();

        runAndCheckExpectations(OneTestDriver.class, DummyTest.class);
    }

    @Test
    public void suite_with_failing_tests() {
        listener.onSuiteStarted();
        listener.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        listener.onTestStarted(RUN_1, DummyTest.class.getName(), TestId.ROOT);
        listener.onFailure(RUN_1, DummyTest.class.getName(), TestId.ROOT, new Exception("dummy failure"));
        listener.onTestFinished(RUN_1, DummyTest.class.getName(), TestId.ROOT);
        listener.onSuiteFinished();

        runAndCheckExpectations(OneFailingTestDriver.class, DummyTest.class);
    }

    // TODO: many runs

    // guinea pigs

    private static class DummyTest {
    }

    static class ZeroTestsDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
        }
    }

    static class OneTestDriver implements Driver {
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                public void run() {
                    TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
                    tn.fireTestFinished();
                }
            });
        }
    }

    static class OneFailingTestDriver implements Driver {
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                public void run() {
                    TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
                    tn.fireFailure(new Exception("dummy failure"));
                    tn.fireTestFinished();
                }
            });
        }
    }


    // helpers

    private void runAndCheckExpectations(Class<? extends Driver> driverClass, Class<?>... testClasses) {
        spy.replay();
        TestClassFinder testClassFinder = new StubTestClassFinder(testClasses);
        DriverFinder driverFinder = new StubDriverFinder(driverClass);
        SuiteRunner runner = new SuiteRunner(listener, testClassFinder, driverFinder, actors, executor);
        actors.createPrimaryActor(Startable.class, runner, "SuiteRunner").start();
        actors.processEventsUntilIdle();
        spy.verify();
    }

    private static class StubDriverFinder implements DriverFinder {
        private final Class<? extends Driver> driverClass;

        public StubDriverFinder(Class<? extends Driver> driverClass) {
            this.driverClass = driverClass;
        }

        public Class<? extends Driver> findTestClassDriver(Class<?> testClass) {
            return driverClass;
        }
    }

    private static class StubTestClassFinder implements TestClassFinder {
        private final Class<?>[] testClasses;

        public StubTestClassFinder(Class<?>... testClasses) {
            this.testClasses = testClasses;
        }

        public void findTestClasses(TestClassFinderListener listener) {
            for (Class<?> testClass : testClasses) {
                listener.onTestClassFound(testClass);
            }
        }
    }
}
