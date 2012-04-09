// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;
import fi.jumi.core.RunIdSequence;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DefaultSuiteNotifier implements SuiteNotifier {

    private final TestClassListener listener;
    private final CurrentRun currentRun;

    public DefaultSuiteNotifier(TestClassListener listener, RunIdSequence runIdSequence) {
        this.listener = listener;
        this.currentRun = new CurrentRun(runIdSequence);
    }

    public void fireTestFound(TestId id, String name) {
        listener.onTestFound(id, name);
    }

    public TestNotifier fireTestStarted(TestId id) {
        currentRun.enterTest();
        listener.onTestStarted(currentRun.getRunId(), id);
        return new DefaultTestNotifier(currentRun, id, listener);
    }
}
