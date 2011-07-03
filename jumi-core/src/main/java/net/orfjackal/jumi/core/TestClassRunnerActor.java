// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.*;
import net.orfjackal.jumi.core.actors.Actors;

import java.util.concurrent.*;

public class TestClassRunnerActor implements Runnable, WorkerListener {

    private final Class<?> testClass;
    private final Class<? extends Driver> driverClass;
    private final SuiteListener listener;
    private final Executor executor;
    private final Actors actors;

    public TestClassRunnerActor(Class<?> testClass,
                                Class<? extends Driver> driverClass,
                                SuiteListener listener,
                                ExecutorService executor,
                                Actors actors) {
        this.testClass = testClass;
        this.driverClass = driverClass;
        this.listener = listener;
        this.executor = executor;
        this.actors = actors;
    }

    public void run() {
        listener.onTestClassStarted(testClass);

        WorkerListener workerListener = actors.bindToCurrentActor(WorkerListener.class, this);

        startWorker(new DriverRunner(new TestClassRunner(listener, testClass).getSuiteNotifier()), workerListener);
    }

    private void startWorker(Runnable worker, WorkerListener workerListener) {
        onWorkerStarted();
        executor.execute(new WorkerFinishedNotifier(worker, workerListener));
    }

    public void onWorkerStarted() {
        // TODO: keep count of how many workers there are
    }

    public void onWorkerFinished() {
        // TODO: fire this event only after all workers are finished
        listener.onTestClassFinished(testClass);
    }


    private class DriverRunner implements Runnable {
        private final SuiteNotifier suiteNotifier;

        public DriverRunner(SuiteNotifier suiteNotifier) {
            this.suiteNotifier = suiteNotifier;
        }

        public void run() {
            // TODO: pass an executor which keeps a count of how many workers there are
            newDriverInstance().findTests(testClass, suiteNotifier, null);
        }

        private Driver newDriverInstance() {
            try {
                return driverClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class WorkerFinishedNotifier implements Runnable {
        private final Runnable worker;
        private final WorkerListener workerListener;

        public WorkerFinishedNotifier(Runnable worker, WorkerListener workerListener) {
            this.worker = worker;
            this.workerListener = workerListener;
        }

        public void run() {
            worker.run();

            // TODO: should call also on failure (need a try-finally, maybe in a wrapper class)
            workerListener.onWorkerFinished();
        }
    }
}
