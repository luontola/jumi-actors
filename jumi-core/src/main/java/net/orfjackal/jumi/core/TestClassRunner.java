// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.*;
import net.orfjackal.jumi.core.actors.OnDemandActors;

import java.util.*;

public class TestClassRunner implements Startable {

    private final Class<?> testClass;
    private final Class<? extends Driver> driverClass;
    private final TestClassRunnerListener listener;
    private final OnDemandActors actors;
    private final Map<TestId, String> tests = new HashMap<TestId, String>();

    public TestClassRunner(Class<?> testClass,
                           Class<? extends Driver> driverClass,
                           TestClassRunnerListener listener,
                           OnDemandActors actors) {
        this.testClass = testClass;
        this.driverClass = driverClass;
        this.listener = listener;
        this.actors = actors;
    }

    public void start() {
        SuiteNotifier notifier = getSuiteNotifier();
        DriverRunner worker = new DriverRunner(notifier);

        actors.startUnattendedWorker(worker, new Runnable() {
            public void run() {
                // TODO: count workers, fire "onTestClassFinished" only after all workers are finished
                listener.onTestClassFinished();
            }
        });
    }

    public Collection<String> getTestNames() {
        // XXX: smelly getter; remove it and move the responsibility somewhere else
        return Collections.unmodifiableCollection(tests.values());
    }

    public SuiteNotifier getSuiteNotifier() {
        return new DefaultSuiteNotifier(this);
    }

    public void onTestFound(TestId id, String name) {
        if (hasNotBeenFoundBefore(id)) {
            checkParentWasFoundFirst(id);
            tests.put(id, name);
            listener.onTestFound(id, name);
        } else {
            checkNameIsSameAsBefore(id, name);
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


    // TODO: decouple DriverRunner from TestClassRunner (at least once long-lived drivers are added)
    private class DriverRunner implements Runnable {
        private final SuiteNotifier suiteNotifier;

        public DriverRunner(SuiteNotifier suiteNotifier) {
            this.suiteNotifier = suiteNotifier;
        }

        public void run() {
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
}
