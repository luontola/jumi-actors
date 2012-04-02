// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.Driver;
import fi.jumi.core.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.events.executor.*;
import fi.jumi.core.events.runnable.RunnableFactory;
import fi.jumi.core.events.startable.StartableFactory;
import fi.jumi.core.events.testclass.TestClassListenerFactory;
import fi.jumi.core.events.testclassfinder.TestClassFinderListenerFactory;
import fi.jumi.core.files.*;

import java.util.concurrent.Executor;

public abstract class SuiteRunnerIntegrationHelper {

    private final SpyListener<SuiteListener> spy = new SpyListener<SuiteListener>(SuiteListener.class);
    protected final SuiteListener listener = spy.getListener();

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new StartableFactory(),
            new RunnableFactory(),
            new ExecutorFactory(),
            new ExecutorListenerFactory(),
            new TestClassFinderListenerFactory(),
            new TestClassListenerFactory()
    );
    private final Executor executor = new SynchronousExecutor();

    protected void runAndCheckExpectations(Class<? extends Driver> driverClass, Class<?>... testClasses) {
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
