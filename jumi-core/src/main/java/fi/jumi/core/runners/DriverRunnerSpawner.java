// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.*;
import fi.jumi.api.drivers.*;
import fi.jumi.core.runs.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class DriverRunnerSpawner {

    private final WorkerCounter workerCounter;
    private final OnDemandActors actors;
    private final TestRunSpawner testRunSpawner;
    private final RunIdSequence runIdSequence;

    private final TestClassListener rawTarget;

    public DriverRunnerSpawner(WorkerCounter workerCounter, OnDemandActors actors, TestRunSpawner testRunSpawner, RunIdSequence runIdSequence, TestClassListener rawTarget) {
        this.actors = actors;
        this.workerCounter = workerCounter;
        this.testRunSpawner = testRunSpawner;
        this.runIdSequence = runIdSequence;
        this.rawTarget = rawTarget;
    }

    public void spawnDriverRunner(Driver driver, Class<?> testClass) {
        ActorRef<TestClassListener> target = actors.createSecondaryActor(TestClassListener.class, rawTarget);

        SuiteNotifier notifier = new DefaultSuiteNotifier(target, runIdSequence);
        Executor executor = testRunSpawner.getExecutor();
        spawnWorker(new DriverRunner(driver, testClass, notifier, executor));
    }

    private void spawnWorker(DriverRunner worker) {
        @NotThreadSafe
        class OnWorkerFinished implements Runnable {
            @Override
            public void run() {
                workerCounter.fireWorkerFinished();
            }
        }
        workerCounter.fireWorkerStarted();
        actors.startUnattendedWorker(worker, new OnWorkerFinished());
    }
}
