// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.api.drivers;

public interface TestNotifier {

    /**
     * May be called multiple times per test (although most testing frameworks
     * will stop execution on the first exception).
     */
    void fireFailure(Throwable cause);

    /**
     * Must be called last, exactly once. The test will fail if fireFailure() had
     * been called at least once, otherwise the test will pass. It is a runtime error
     * to call fireFailure() or fireTestFinished() after calling this method. If the
     * test started any threads, will wait for them to finish (except the AWT event
     * thread and maybe some others). It is an error (or at least a warning) for a
     * test to start threads without stopping them.
     */
    void fireTestFinished();
}
