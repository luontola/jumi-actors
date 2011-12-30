// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;
import fi.jumi.api.drivers.Driver;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.Startable;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.files.TestClassFinder;
import fi.jumi.core.files.TestClassFinderListener;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SuiteRunner implements Startable, TestClassFinderListener {

    private final SuiteListener listener;
    private final TestClassFinder testClassFinder;
    private final DriverFinder driverFinder;
    private final OnDemandActors actors;
    private final Executor executor;
    private int workers = 0;

    public SuiteRunner(SuiteListener listener,
                       TestClassFinder testClassFinder,
                       DriverFinder driverFinder,
                       OnDemandActors actors,
                       Executor executor) {
        this.listener = listener;
        this.testClassFinder = testClassFinder;
        this.driverFinder = driverFinder;
        this.actors = actors;
        this.executor = executor;
    }

    public void start() {
        // XXX: this call might not be needed (it could even be harmful because of asynchrony); the caller of SuiteRunner knows when the suite is started
        listener.onSuiteStarted();

        final TestClassFinderListener finderListener = actors.createSecondaryActor(TestClassFinderListener.class, this);
        startUnattendedWorker(new TestClassFinderRunner(testClassFinder, finderListener));
    }

    private void startUnattendedWorker(Runnable worker) {
        fireWorkerStarted();

        @NotThreadSafe
        class FireWorkerFinished implements Runnable {
            public void run() {
                fireWorkerFinished();
            }
        }
        actors.startUnattendedWorker(worker, new FireWorkerFinished());
    }

    private void fireWorkerStarted() {
        workers++;
    }

    private void fireWorkerFinished() {
        workers--;
        assert workers >= 0;
        if (workers == 0) {
            listener.onSuiteFinished();
        }
    }

    public void onTestClassFound(final Class<?> testClass) {
        Class<? extends Driver> driverClass = driverFinder.findTestClassDriver(testClass);

        fireWorkerStarted();
        new TestClassRunner(
                testClass, driverClass, new TestClassRunnerListenerToSuiteListener(testClass), actors, executor
        ).start();
    }

    @NotThreadSafe
    private class TestClassRunnerListenerToSuiteListener implements TestClassRunnerListener {
        private final Class<?> testClass;

        public TestClassRunnerListenerToSuiteListener(Class<?> testClass) {
            this.testClass = testClass;
        }

        public void onTestFound(TestId id, String name) {
            listener.onTestFound(testClass.getName(), id, name);
        }

        public void onTestStarted(TestId id) {
            listener.onTestStarted(testClass.getName(), id);
        }

        public void onFailure(TestId id, Throwable cause) {
            listener.onFailure(testClass.getName(), id, cause);
        }

        public void onTestFinished(TestId id) {
            listener.onTestFinished(testClass.getName(), id);
        }

        public void onTestClassFinished() {
            fireWorkerFinished();
        }
    }

    @ThreadSafe
    private static class TestClassFinderRunner implements Runnable {
        private final TestClassFinderListener finderListener;
        private TestClassFinder testClassFinder;

        public TestClassFinderRunner(TestClassFinder testClassFinder, TestClassFinderListener finderListener) {
            this.finderListener = finderListener;
            this.testClassFinder = testClassFinder;
        }

        public void run() {
            testClassFinder.findTestClasses(finderListener);
        }
    }
}
