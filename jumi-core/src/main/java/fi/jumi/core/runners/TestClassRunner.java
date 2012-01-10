// Copyright © 2011, Esko Luontola <www.orfjackal.net>
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
public class TestClassRunner implements Startable, TestClassListener {

    private final Class<?> testClass;
    private final Class<? extends Driver> driverClass;
    private final TestClassRunnerListener listener;
    private final OnDemandActors actors;
    private final Map<TestId, String> tests = new HashMap<TestId, String>();
    private final Executor realExecutor;
    private int workers = 0;

    public TestClassRunner(Class<?> testClass,
                           Class<? extends Driver> driverClass,
                           TestClassRunnerListener listener,
                           OnDemandActors actors,
                           Executor executor) {
        this.testClass = testClass;
        this.driverClass = driverClass;
        this.listener = listener;
        this.actors = actors;
        this.realExecutor = executor;
    }

    public void start() {
        TestClassListener self = actors.createSecondaryActor(TestClassListener.class, this);

        Executor myExecutor = new QueuingExecutor(self);
        SuiteNotifier notifier = new DefaultSuiteNotifier(self);
        DriverRunner worker = new DriverRunner(testClass, driverClass, notifier, myExecutor);

        @NotThreadSafe
        class OnDriverFinished implements Runnable {
            public void run() {
                fireWorkerFinished();
            }
        }
        fireWorkerStarted();
        actors.startUnattendedWorker(worker, new OnDriverFinished());
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

    public void onExecutorCommandQueued(final Runnable runnable) {
        fireWorkerStarted();
        final TestClassListener self = actors.createSecondaryActor(TestClassListener.class, this);

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
        realExecutor.execute(new OnFinishedNotifier());
    }

    public void onExecutorCommandFinished() {
        fireWorkerFinished();
    }

    private void fireWorkerStarted() {
        workers++;
    }

    private void fireWorkerFinished() {
        workers--;
        assert workers >= 0;
        if (workers == 0) {
            listener.onTestClassFinished();
        }
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

    @ThreadSafe
    private static class QueuingExecutor implements Executor { // TODO: make TestClassRunner implement Executor directly?

        private final TestClassListener target;

        private QueuingExecutor(TestClassListener target) {
            this.target = target;
        }

        public void execute(Runnable command) {
            target.onExecutorCommandQueued(command);
        }
    }
}
