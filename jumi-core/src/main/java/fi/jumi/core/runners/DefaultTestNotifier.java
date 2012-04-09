// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DefaultTestNotifier implements TestNotifier {

    private final CurrentRun currentRun;
    private final TestId id;
    private final TestClassListener listener;

    public DefaultTestNotifier(CurrentRun currentRun, TestId id, TestClassListener listener) {
        this.currentRun = currentRun;
        this.id = id;
        this.listener = listener;
    }

    public void fireFailure(Throwable cause) {
        listener.onFailure(id, cause);
    }

    public void fireTestFinished() {
        currentRun.exitTest(); // TODO: onRunFinished should be after onTestFinished
        listener.onTestFinished(id);
    }
}
