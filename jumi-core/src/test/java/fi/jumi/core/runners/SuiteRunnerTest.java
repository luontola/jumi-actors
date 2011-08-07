// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.actors.dynamicevents.DynamicListenerFactory;
import fi.jumi.core.files.*;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

public class SuiteRunnerTest {

    private final SuiteListener listener = mock(SuiteListener.class);
    private final InOrder inOrder = inOrder(listener);

    private final SingleThreadedActors actors = new SingleThreadedActors(
            DynamicListenerFactory.factoriesFor(Startable.class, Runnable.class, TestClassFinderListener.class));

    @Test
    public void suite_with_zero_test_classes() throws InterruptedException {
        TestClassFinder testClassFinder = new TestClassFinder() {
            public void findTestClasses(TestClassFinderListener listener) {
            }
        };
        DriverFinder driverFinder = null;
        SuiteRunner runner = new SuiteRunner(listener, testClassFinder, driverFinder, actors);

        runAndAwaitCompletion(runner);

        inOrder.verify(listener).onSuiteStarted();
        inOrder.verify(listener).onSuiteFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void suite_with_one_test_class_with_zero_tests() throws InterruptedException {
        TestClassFinder finder = new TestClassFinder() {
            public void findTestClasses(TestClassFinderListener listener) {
                listener.onTestClassFound(DummyTest.class);
            }
        };
        DriverFinder driverFinder = new DriverFinder() {
            public Class<? extends Driver> findTestClassDriver(Class<?> testClass) {
                return ZeroTestsDriver.class;
            }
        };
        SuiteRunner runner = new SuiteRunner(listener, finder, driverFinder, actors);

        runAndAwaitCompletion(runner);

        inOrder.verify(listener).onSuiteStarted();
        inOrder.verify(listener).onSuiteFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void suite_with_one_test_class_with_tests() throws InterruptedException {
        TestClassFinder finder = new TestClassFinder() {
            public void findTestClasses(TestClassFinderListener listener) {
                listener.onTestClassFound(DummyTest.class);
            }
        };
        DriverFinder driverFinder = new DriverFinder() {
            public Class<? extends Driver> findTestClassDriver(Class<?> testClass) {
                return OneTestDriver.class;
            }
        };
        SuiteRunner runner = new SuiteRunner(listener, finder, driverFinder, actors);

        runAndAwaitCompletion(runner);

        inOrder.verify(listener).onSuiteStarted();
        inOrder.verify(listener).onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        inOrder.verify(listener).onSuiteFinished();
        verifyNoMoreInteractions(listener);
    }


    private void runAndAwaitCompletion(SuiteRunner runner) {
        actors.createPrimaryActor(Startable.class, runner, "SuiteRunner").start();
        actors.processEventsUntilIdle();
    }

    private static class DummyTest {
    }

    static class ZeroTestsDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }

    static class OneTestDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
        }
    }
}
