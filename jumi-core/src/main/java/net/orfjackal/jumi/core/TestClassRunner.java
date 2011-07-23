// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.*;
import net.orfjackal.jumi.core.actors.OnDemandActors;

public class TestClassRunner implements Runnable {

    private final Class<?> testClass;
    private final Class<? extends Driver> driverClass;
    private final SuiteListener listener;
    private final OnDemandActors actors;

    public TestClassRunner(Class<?> testClass,
                           Class<? extends Driver> driverClass,
                           SuiteListener listener,
                           OnDemandActors actors) {
        this.testClass = testClass;
        this.driverClass = driverClass;
        this.listener = listener;
        this.actors = actors;
    }

    public void run() {
        listener.onTestClassStarted(testClass);

        SuiteNotifier notifier = new TestClassState(listener, testClass).getSuiteNotifier();
        DriverRunner worker = new DriverRunner(notifier);

        actors.startUnattendedWorker(worker, new OnWorkerFinished());
    }


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

    private class OnWorkerFinished implements Runnable {
        public void run() {
            // TODO: count workers, fire "onTestClassFinished" only after all workers are finished
            listener.onTestClassFinished(testClass);
        }
    }
}
