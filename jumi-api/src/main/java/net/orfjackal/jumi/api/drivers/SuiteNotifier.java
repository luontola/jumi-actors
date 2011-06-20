// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.api.drivers;

public interface SuiteNotifier {

    // TODO: support navigation by providing a Class or Method instance to the fireTestFound method

    /**
     * Must be called before starting the test. Idempotent. IDEs can use the
     * Class or Method object to implement code navigation.
     */
    void fireTestFound(TestId id, String name);

    /**
     * May be called multiple times, before a test is finished, to produce nested tests.
     * The only limitation is that a nested test must be finished before the surrounding tests.
     * Everything printed to System.out and System.err after the call to this method will be
     * recorded from the current thread and threads which are started by it (possibly together
     * with timestamps and name of the thread which printed it).
     */
    TestNotifier fireTestStarted(TestId id);
}
