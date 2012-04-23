// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.*;
import fi.jumi.api.drivers.*;
import fi.jumi.core.Startable;
import fi.jumi.core.runs.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class TestClassRunner implements Startable, TestClassListener, WorkerCounterListener {

    private final Class<?> testClass;
    private final Driver driver;
    private final TestClassRunnerListener target;

    private final DriverRunnerSpawner driverRunnerSpawner;

    public TestClassRunner(Class<?> testClass,
                           Driver driver,
                           TestClassRunnerListener target,
                           Actors actors,
                           ActorThread actorThread,
                           Executor executor,
                           RunIdSequence runIdSequence) {
        this.testClass = testClass;
        this.driver = driver;
        this.target = target;

        WorkerCounter workerCounter = new WorkerCounter(this);
        TestRunSpawner testRunSpawner = new TestRunSpawner(actorThread, workerCounter, executor);
        this.driverRunnerSpawner = new DriverRunnerSpawner(actors, actorThread, workerCounter, testRunSpawner, runIdSequence,
                new DuplicateOnTestFoundEventFilter(this));
    }

    @Override
    public void start() {
        driverRunnerSpawner.spawnDriverRunner(driver, testClass);
    }

    @Override
    public void onAllWorkersFinished() {
        target.onTestClassFinished();
    }

    // convert TestClassListener events to TestClassRunnerListener

    @Override
    public void onTestFound(TestId testId, String name) {
        target.onTestFound(testId, name);
    }

    @Override
    public void onRunStarted(RunId runId) {
        target.onRunStarted(runId);
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        target.onTestStarted(runId, testId);
    }

    @Override
    public void onFailure(RunId runId, TestId testId, Throwable cause) {
        target.onFailure(runId, testId, cause);
    }

    @Override
    public void onTestFinished(RunId runId, TestId testId) {
        target.onTestFinished(runId, testId);
    }

    @Override
    public void onRunFinished(RunId runId) {
        target.onRunFinished(runId);
    }
}
