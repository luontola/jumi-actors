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
    private final Driver driver;
    private final SuiteNotifier suiteNotifier;
    private final Executor executor;

    public DriverRunner(Driver driver, Class<?> testClass, SuiteNotifier suiteNotifier, Executor executor) {
        this.testClass = testClass;
        this.driver = driver;
        this.suiteNotifier = suiteNotifier;
        this.executor = executor;
    }

    public void run() {
        driver.findTests(testClass, suiteNotifier, executor);
    }
}
