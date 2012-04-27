// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.*;
import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.files.*;
import fi.jumi.core.runs.*;

import javax.annotation.concurrent.*;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SuiteRunner implements Startable, TestClassFinderListener, WorkerCounterListener {

    private final SuiteListener listener;
    private final TestClassFinder testClassFinder;
    private final DriverFinder driverFinder;
    private final Actors actors;
    private final ActorThread actorThread;
    private final Executor executor;
    private final WorkerCounter workers;
    private final RunIdSequence runIdSequence = new RunIdSequence();

    public SuiteRunner(SuiteListener listener,
                       TestClassFinder testClassFinder,
                       DriverFinder driverFinder,
                       Actors actors,
                       ActorThread actorThread,
                       Executor executor) {
        this.listener = listener;
        this.testClassFinder = testClassFinder;
        this.driverFinder = driverFinder;
        this.actors = actors;
        this.actorThread = actorThread;
        this.executor = executor;
        this.workers = new WorkerCounter(this);
    }

    @Override
    public void start() {
        // XXX: this call might not be needed (it could even be harmful because of asynchrony); the caller of SuiteRunner knows when the suite is started
        listener.onSuiteStarted();

        ActorRef<TestClassFinderListener> finderListener = actorThread.bindActor(TestClassFinderListener.class, this);
        startUnattendedWorker(new TestClassFinderRunner(testClassFinder, finderListener));
    }

    private void startUnattendedWorker(Runnable worker) {
        workers.fireWorkerStarted();

        @NotThreadSafe
        class FireWorkerFinished implements Runnable {
            @Override
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

    @Override
    public void onTestClassFound(final Class<?> testClass) {
        Driver driver = driverFinder.findTestClassDriver(testClass);

        workers.fireWorkerStarted();
        new TestClassRunner(
                testClass, driver, new TestClassRunnerListenerToSuiteListener(testClass), actors, actorThread, executor, runIdSequence
        ).start();
    }

    @NotThreadSafe
    private class TestClassRunnerListenerToSuiteListener implements TestClassRunnerListener {
        private final Class<?> testClass;

        public TestClassRunnerListenerToSuiteListener(Class<?> testClass) {
            this.testClass = testClass;
        }

        @Override
        public void onTestFound(TestId testId, String name) {
            listener.onTestFound(testClass.getName(), testId, name);
        }

        @Override
        public void onRunStarted(RunId runId) {
            listener.onRunStarted(runId, testClass.getName());
        }

        @Override
        public void onTestStarted(RunId runId, TestId testId) {
            listener.onTestStarted(runId, testId);
        }

        @Override
        public void onFailure(RunId runId, TestId testId, Throwable cause) {
            listener.onFailure(runId, cause);
        }

        @Override
        public void onTestFinished(RunId runId, TestId testId) {
            listener.onTestFinished(runId);
        }

        @Override
        public void onRunFinished(RunId runId) {
            listener.onRunFinished(runId);
        }

        @Override
        public void onTestClassFinished() {
            workers.fireWorkerFinished();
        }
    }

    @ThreadSafe
    private static class TestClassFinderRunner implements Runnable {
        private final ActorRef<TestClassFinderListener> finderListener;
        private final TestClassFinder testClassFinder;

        public TestClassFinderRunner(TestClassFinder testClassFinder, ActorRef<TestClassFinderListener> finderListener) {
            this.finderListener = finderListener;
            this.testClassFinder = testClassFinder;
        }

        @Override
        public void run() {
            testClassFinder.findTestClasses(finderListener);
        }
    }
}
