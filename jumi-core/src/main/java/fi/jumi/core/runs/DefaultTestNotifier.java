// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.api.drivers.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DefaultTestNotifier implements TestNotifier {

    private final CurrentRun currentRun;
    private final TestId testId;

    public DefaultTestNotifier(CurrentRun currentRun, TestId testId) {
        this.currentRun = currentRun;
        this.testId = testId;
    }

    public void fireFailure(Throwable cause) {
        currentRun.fireFailure(testId, cause);
    }

    public void fireTestFinished() {
        currentRun.fireTestFinished(testId);
    }
}
