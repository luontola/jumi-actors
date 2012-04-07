// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.files.*;

import javax.annotation.concurrent.*;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SuiteRunner implements Startable, TestClassFinderListener, WorkerCounterListener {

    private final SuiteListener listener;
    private final TestClassFinder testClassFinder;
    private final DriverFinder driverFinder;
    private final OnDemandActors actors;
    private final Executor executor;
    private final WorkerCounter workers;

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
        this.workers = new WorkerCounter(this);
    }

    public void start() {
        // XXX: this call might not be needed (it could even be harmful because of asynchrony); the caller of SuiteRunner knows when the suite is started
        listener.onSuiteStarted();

        final TestClassFinderListener finderListener = actors.createSecondaryActor(TestClassFinderListener.class, this);
        startUnattendedWorker(new TestClassFinderRunner(testClassFinder, finderListener));
    }

    private void startUnattendedWorker(Runnable worker) {
        workers.fireWorkerStarted();

        @NotThreadSafe
        class FireWorkerFinished implements Runnable {
            public void run() {
                workers.fireWorkerFinished();
            }
        }
        actors.startUnattendedWorker(worker, new FireWorkerFinished());
    }

    @Override
    public void onAllWorkersFinished() {
        listener.onSuiteFinished();
    }

    public void onTestClassFound(final Class<?> testClass) {
        Class<? extends Driver> driverClass = driverFinder.findTestClassDriver(testClass);

        workers.fireWorkerStarted();
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
            listener.onTestStarted(new RunId(42), testClass.getName(), id); // TODO: get real runId
        }

        public void onFailure(TestId id, Throwable cause) {
            listener.onFailure(new RunId(42), testClass.getName(), id, cause); // TODO: get real runId
        }

        public void onTestFinished(TestId id) {
            listener.onTestFinished(new RunId(42), testClass.getName(), id); // TODO: get real runId
        }

        public void onTestClassFinished() {
            workers.fireWorkerFinished();
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
