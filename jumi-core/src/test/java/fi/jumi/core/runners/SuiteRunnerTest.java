// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.events.runnable.RunnableFactory;
import fi.jumi.core.events.startable.StartableFactory;
import fi.jumi.core.events.testclass.TestClassListenerFactory;
import fi.jumi.core.events.testclassfinder.TestClassFinderListenerFactory;
import fi.jumi.core.files.*;
import org.junit.Test;

import java.util.concurrent.Executor;

public class SuiteRunnerTest {

    private final SpyListener<SuiteListener> spy = new SpyListener<SuiteListener>(SuiteListener.class);
    private final SuiteListener listener = spy.getListener();

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new StartableFactory(),
            new RunnableFactory(),
            new TestClassFinderListenerFactory(),
            new TestClassListenerFactory()
    );

    @Test
    public void suite_with_zero_test_classes() {
        listener.onSuiteStarted();
        listener.onSuiteFinished();

        runAndCheckExpectations(null, new TestClassFinder() {
            public void findTestClasses(TestClassFinderListener listener) {
            }
        });
    }

    @Test
    public void suite_with_one_test_class_with_zero_tests() {
        listener.onSuiteStarted();
        listener.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        listener.onSuiteFinished();

        runAndCheckExpectations(ZeroTestsDriver.class, new TestClassFinder() {
            public void findTestClasses(TestClassFinderListener listener) {
                listener.onTestClassFound(DummyTest.class);
            }
        });
    }

    @Test
    public void suite_with_one_test_class_with_tests() {
        listener.onSuiteStarted();
        listener.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        listener.onTestStarted(DummyTest.class.getName(), TestId.ROOT);
        listener.onTestFinished(DummyTest.class.getName(), TestId.ROOT);
        listener.onSuiteFinished();

        runAndCheckExpectations(OneTestDriver.class, new TestClassFinder() {
            public void findTestClasses(TestClassFinderListener listener) {
                listener.onTestClassFound(DummyTest.class);
            }
        });
    }

    @Test
    public void suite_with_failing_tests() {
        listener.onSuiteStarted();
        listener.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        listener.onTestStarted(DummyTest.class.getName(), TestId.ROOT);
        listener.onFailure(DummyTest.class.getName(), TestId.ROOT, new Exception("dummy failure"));
        listener.onTestFinished(DummyTest.class.getName(), TestId.ROOT);
        listener.onSuiteFinished();

        runAndCheckExpectations(OneFailingTestDriver.class, new TestClassFinder() {
            public void findTestClasses(TestClassFinderListener listener) {
                listener.onTestClassFound(DummyTest.class);
            }
        });
    }


    // helpers

    private void runAndCheckExpectations(Class<? extends Driver> driverClass, TestClassFinder testClassFinder) {
        spy.replay();
        SuiteRunner runner = new SuiteRunner(listener, testClassFinder, new StubDriverFinder(driverClass), actors);
        actors.createPrimaryActor(Startable.class, runner, "SuiteRunner").start();
        actors.processEventsUntilIdle();
        spy.verify();
    }


    // guinea pigs

    private static class DummyTest {
    }

    static class ZeroTestsDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
        }
    }

    static class OneTestDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            // TODO: use executor
            TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
            tn.fireTestFinished();
        }
    }

    static class OneFailingTestDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            // TODO: use executor
            TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
            tn.fireFailure(new Exception("dummy failure"));
            tn.fireTestFinished();
        }
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
}
