// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

@ThreadSafe
public class SuiteStateCollector implements SuiteListener {

    private volatile SuiteResults state = new SuiteResults();
    private final CountDownLatch finished = new CountDownLatch(1);

    public SuiteResults getState() {
        return state;
    }

    public void onSuiteStarted() {
    }

    public void onSuiteFinished() {
        state = state.withFinished(true);
        finished.countDown();
    }

    public void onTestFound(String testClass, TestId id, String name) {
        state = state.withTest(testClass, id, name);
    }

    public void onTestStarted(String testClass, TestId id) {
        // TODO
    }

    public void onTestFinished(String testClass, TestId id) {
        // TODO
    }

    public void onFailure(String testClass, TestId id, Throwable cause) {
        state = state.withFailure(testClass, id, cause);
    }

    public void awaitSuiteFinished() throws InterruptedException {
        finished.await();
    }

    public boolean awaitSuiteFinished(long timeout, TimeUnit unit) throws InterruptedException {
        return finished.await(timeout, unit);
    }
}
