// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.Startable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class TestClassRunner implements Startable, TestClassListener, WorkerCounterListener {

    private final Class<?> testClass;
    private final Class<? extends Driver> driverClass;
    private final TestClassRunnerListener target;

    private final DriverRunnerSpawner driverRunnerSpawner;

    public TestClassRunner(Class<?> testClass,
                           Class<? extends Driver> driverClass,
                           TestClassRunnerListener target,
                           OnDemandActors actors,
                           Executor executor) {
        this.testClass = testClass;
        this.driverClass = driverClass;
        this.target = target;

        WorkerCounter workerCounter = new WorkerCounter(this);
        TestRunSpawner testRunSpawner = new TestRunSpawner(workerCounter, actors, executor);
        this.driverRunnerSpawner = new DriverRunnerSpawner(workerCounter, actors, testRunSpawner,
                new DuplicateOnTestFoundEventFilter(this));
    }

    @Override
    public void start() {
        driverRunnerSpawner.spawnDriverRunner(testClass, driverClass);
    }

    @Override
    public void onAllWorkersFinished() {
        target.onTestClassFinished();
    }

    // convert TestClassListener events to TestClassRunnerListener

    @Override
    public void onTestFinished(TestId id) {
        target.onTestFinished(id);
    }

    @Override
    public void onFailure(TestId id, Throwable cause) {
        target.onFailure(id, cause);
    }

    @Override
    public void onTestStarted(TestId id) {
        target.onTestStarted(id);
    }

    @Override
    public void onTestFound(TestId id, String name) {
        target.onTestFound(id, name);
    }
}
