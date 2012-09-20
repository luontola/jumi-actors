// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.api.drivers;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

/**
 * Each testing framework should provide its own {@code Driver} implementation so that the Jumi test runner will know
 * how to run tests written using that testing framework.
 */
@NotThreadSafe
public abstract class Driver {

    /**
     * The {@code Executor} should be used to run the tests, so that they can be executed in parallel, each test in a
     * different thread. If the {@code Runnable}s passed to the {@code Executor} are {@code Serializable}, then each of
     * the tests in one class could potentially be executed on different machine in a server cluster. Otherwise any
     * potential clustering is at class-granularity (which may be a hindrance for classes with many slow tests).
     */
    public abstract void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor);
}
