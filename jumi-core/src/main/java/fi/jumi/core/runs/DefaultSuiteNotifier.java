// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.api.drivers.*;
import fi.jumi.core.runners.TestClassListener;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DefaultSuiteNotifier implements SuiteNotifier {

    private final CurrentRun currentRun;

    public DefaultSuiteNotifier(TestClassListener listener, RunIdSequence runIdSequence) {
        this.currentRun = new CurrentRun(listener, runIdSequence);
    }

    public void fireTestFound(TestId testId, String name) {
        currentRun.fireTestFound(testId, name);
    }

    public TestNotifier fireTestStarted(TestId testId) {
        currentRun.fireTestStarted(testId);
        return new DefaultTestNotifier(currentRun, testId);
    }
}
