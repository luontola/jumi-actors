// Copyright © 2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class DriverRunner implements Runnable {

    private final Class<?> testClass;
    private final Class<? extends Driver> driverClass;
    private final SuiteNotifier suiteNotifier;
    private final Executor executor;

    public DriverRunner(Class<?> testClass, Class<? extends Driver> driverClass, SuiteNotifier suiteNotifier, Executor executor) {
        this.testClass = testClass;
        this.driverClass = driverClass;
        this.suiteNotifier = suiteNotifier;
        this.executor = executor;
    }

    public void run() {
        newDriverInstance().findTests(testClass, suiteNotifier, executor);
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
