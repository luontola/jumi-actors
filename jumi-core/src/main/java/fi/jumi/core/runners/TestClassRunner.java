// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
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
                           OnDemandActors actors,
                           Executor executor,
                           RunIdSequence runIdSequence) {
        this.testClass = testClass;
        this.driver = driver;
        this.target = target;

        WorkerCounter workerCounter = new WorkerCounter(this);
        TestRunSpawner testRunSpawner = new TestRunSpawner(workerCounter, actors, executor);
        this.driverRunnerSpawner = new DriverRunnerSpawner(workerCounter, actors, testRunSpawner, runIdSequence,
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
    public void onTestFound(TestId id, String name) {
        target.onTestFound(id, name);
    }

    @Override
    public void onRunStarted(RunId runId) {
        // TODO
    }

    @Override
    public void onTestStarted(RunId runId, TestId id) {
        target.onTestStarted(id);
    }

    @Override
    public void onFailure(TestId id, Throwable cause) {
        target.onFailure(id, cause);
    }

    @Override
    public void onTestFinished(TestId id) {
        target.onTestFinished(id);
    }

    @Override
    public void onRunFinished(RunId runId) {
        // TODO
    }
}
