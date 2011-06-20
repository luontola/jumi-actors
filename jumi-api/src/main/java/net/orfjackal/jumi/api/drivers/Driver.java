// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.api.drivers;

/**
 * Each testing framework should provide its own {@code Driver} implementation so that the test runner
 * can run tests written using that framework.
 */
public interface Driver {

    /**
     * The Executor should be used to run the tests, so that they can be executed in parallel,
     * each test in a different thread. If the Runnables passed to the Executor are Serializable,
     * then each of the tests in one class could potentially be executed on different machine in
     * a server cluster. Otherwise any potential clustering is at class-granularity (which may be
     * a hindrance for classes with many slow tests).
     */
    void findTests(Class<?> testClass, SuiteNotifier notifier, java.util.concurrent.Executor executor);
}
