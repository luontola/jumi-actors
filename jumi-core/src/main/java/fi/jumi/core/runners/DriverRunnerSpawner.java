// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;
import fi.jumi.api.drivers.*;

import javax.annotation.concurrent.*;
import java.util.concurrent.Executor;

@NotThreadSafe
public class DriverRunnerSpawner implements ExecutorListener {

    private final OnDemandActors actors;
    private final Executor testRunExecutor;
    private final WorkerCounter workerCounter;
    private final TestClassRunner rawTarget;

    public DriverRunnerSpawner(OnDemandActors actors, Executor testRunExecutor, WorkerCounter workerCounter, TestClassRunner rawTarget) {
        this.actors = actors;
        this.testRunExecutor = testRunExecutor;
        this.workerCounter = workerCounter;
        this.rawTarget = rawTarget;
    }

    public void spawnDriverRunner(Class<?> testClass, Class<? extends Driver> driverClass) {
        TestClassListener target = actors.createSecondaryActor(TestClassListener.class, rawTarget);
        ExecutorListener executorListener = actors.createSecondaryActor(ExecutorListener.class, this);

        Executor testRunExecutor = new TestRunExecutor(executorListener); // TODO: extract TestRunSpawner
        SuiteNotifier notifier = new DefaultSuiteNotifier(target);
        spawnWorker(new DriverRunner(testClass, driverClass, notifier, testRunExecutor));
    }

    private void spawnWorker(DriverRunner worker) {
        @NotThreadSafe
        class OnWorkerFinished implements Runnable {
            public void run() {
                workerCounter.fireWorkerFinished();
            }
        }
        workerCounter.fireWorkerStarted();
        actors.startUnattendedWorker(worker, new OnWorkerFinished());
    }

    public void onExecutorCommandQueued(final Runnable runnable) {
        workerCounter.fireWorkerStarted();
        final ExecutorListener self = actors.createSecondaryActor(ExecutorListener.class, this);

        // TODO: extract TestRunSpawner
        @NotThreadSafe
        class OnFinishedNotifier implements Runnable {
            public void run() {
                try {
                    runnable.run();
                } finally {
                    self.onExecutorCommandFinished();
                }
            }
        }
        testRunExecutor.execute(new OnFinishedNotifier());
    }

    public void onExecutorCommandFinished() {
        workerCounter.fireWorkerFinished();
    }

    @ThreadSafe
    private static class TestRunExecutor implements Executor { // TODO: make TestClassRunner implement Executor directly?

        private final ExecutorListener target;

        private TestRunExecutor(ExecutorListener target) {
            this.target = target;
        }

        public void execute(Runnable command) {
            target.onExecutorCommandQueued(command);
        }
    }
}
