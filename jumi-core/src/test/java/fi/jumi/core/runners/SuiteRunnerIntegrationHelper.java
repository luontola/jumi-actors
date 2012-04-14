// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.Driver;
import fi.jumi.core.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.events.*;
import fi.jumi.core.files.*;
import fi.jumi.core.utils.SpyListener;

import java.util.concurrent.Executor;

public abstract class SuiteRunnerIntegrationHelper {

    private final SpyListener<SuiteListener> spy = new SpyListener<SuiteListener>(SuiteListener.class);
    protected final SuiteListener expect = spy.getListener();

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new StartableFactory(),
            new RunnableFactory(),
            new ExecutorFactory(),
            new ExecutorListenerFactory(),
            new TestClassFinderListenerFactory(),
            new TestClassListenerFactory()
    );
    private final Executor executor = new SynchronousExecutor();

    protected void runAndCheckExpectations(Driver driver, Class<?>... testClasses) {
        spy.replay();
        run(driver, testClasses);
        spy.verify();
    }

    protected void run(Driver driver, Class<?>... testClasses) {
        run(new StubDriverFinder(driver), testClasses);
    }

    protected void run(DriverFinder driverFinder, Class<?>... testClasses) {
        run(expect, driverFinder, testClasses);
    }

    protected void run(SuiteListener listener, Driver driver, Class<?>... testClasses) {
        run(listener, new StubDriverFinder(driver), testClasses);
    }

    protected void run(SuiteListener listener, DriverFinder driverFinder, Class<?>... testClasses) {
        TestClassFinder testClassFinder = new StubTestClassFinder(testClasses);
        SuiteRunner runner = new SuiteRunner(listener, testClassFinder, driverFinder, actors, executor);
        actors.createPrimaryActor(Startable.class, runner, "SuiteRunner").start();
        actors.processEventsUntilIdle();
    }

    private static class StubDriverFinder implements DriverFinder {
        private final Driver driver;

        public StubDriverFinder(Driver driver) {
            this.driver = driver;
        }

        public Driver findTestClassDriver(Class<?> testClass) {
            return driver;
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
