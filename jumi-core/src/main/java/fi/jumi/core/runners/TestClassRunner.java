// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.Startable;

import javax.annotation.concurrent.*;
import java.util.*;
import java.util.concurrent.Executor;

@NotThreadSafe
public class TestClassRunner implements Startable, TestClassListener, WorkerCounterListener {

    private final Class<?> testClass;
    private final Class<? extends Driver> driverClass;
    private final TestClassRunnerListener listener;
    private final Map<TestId, String> tests = new HashMap<TestId, String>();

    private DriverRunnerSpawner driverRunnerSpawner;

    public TestClassRunner(Class<?> testClass,
                           Class<? extends Driver> driverClass,
                           TestClassRunnerListener listener,
                           OnDemandActors actors,
                           Executor executor) {
        this.testClass = testClass;
        this.driverClass = driverClass;
        this.listener = listener;

        WorkerCounter workerCounter = new WorkerCounter(this);
        this.driverRunnerSpawner = new DriverRunnerSpawner(actors, executor, workerCounter, this);
    }

    public void start() {
        driverRunnerSpawner.spawnDriverRunner(testClass, driverClass);
    }

    public void onTestFound(TestId id, String name) {
        // TODO: is this worthwhile? move to SuiteRunner.TestClassRunnerListenerToSuiteListener?
        if (hasNotBeenFoundBefore(id)) {
            checkParentWasFoundFirst(id);
            tests.put(id, name);
            listener.onTestFound(id, name);
        } else {
            checkNameIsSameAsBefore(id, name);
        }
    }

    public void onTestStarted(TestId id) {
        listener.onTestStarted(id);
    }

    public void onFailure(TestId id, Throwable cause) {
        listener.onFailure(id, cause);
    }

    public void onTestFinished(TestId id) {
        listener.onTestFinished(id);
    }

    public void onAllWorkersFinished() {
        listener.onTestClassFinished();
    }

    private boolean hasNotBeenFoundBefore(TestId id) {
        return !tests.containsKey(id);
    }

    private void checkParentWasFoundFirst(TestId id) {
        if (!id.isRoot() && hasNotBeenFoundBefore(id.getParent())) {
            throw new IllegalStateException("parent of " + id + " must be found first");
        }
    }

    private void checkNameIsSameAsBefore(TestId id, String newName) {
        String oldName = tests.get(id);
        if (oldName != null && !oldName.equals(newName)) {
            throw new IllegalStateException("test " + id + " was already found with another name: " + oldName);
        }
    }

}
