// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runners.TestClassListener;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
public class CurrentRun {

    private final TestClassListener listener;
    private final RunIdSequence runIdSequence;
    private final InheritableThreadLocal<RunContext> currentRun = new InheritableThreadLocal<RunContext>();

    public CurrentRun(TestClassListener listener, RunIdSequence runIdSequence) {
        this.listener = listener;
        this.runIdSequence = runIdSequence;
    }

    public void fireTestFound(TestId testId, String name) {
        listener.onTestFound(testId, name);
    }

    public void fireTestStarted(TestId testId) {
        RunContext currentRun = this.currentRun.get();
        if (currentRun == null) {
            currentRun = new RunContext(runIdSequence.nextRunId());
            this.currentRun.set(currentRun);

            listener.onRunStarted(currentRun.runId);
        }
        currentRun.countTestStarted();
        listener.onTestStarted(currentRun.runId, testId);
    }

    public void fireFailure(TestId testId, Throwable cause) {
        RunContext currentRun = this.currentRun.get();
        listener.onFailure(currentRun.runId, testId, cause);
    }

    public void fireTestFinished(TestId testId) {
        RunContext currentRun = this.currentRun.get();

        listener.onTestFinished(currentRun.runId, testId);
        currentRun.countTestFinished();

        if (currentRun.isRunFinished()) {
            this.currentRun.remove();
            listener.onRunFinished(currentRun.runId);
        }
    }


    @ThreadSafe
    private static class RunContext {
        public final RunId runId;
        private final AtomicInteger testNestingLevel = new AtomicInteger(0);

        public RunContext(RunId runId) {
            this.runId = runId;
        }

        public void countTestStarted() {
            testNestingLevel.incrementAndGet();
        }

        public void countTestFinished() {
            int level = testNestingLevel.decrementAndGet();
            assert level >= 0;
        }

        public boolean isRunFinished() {
            return testNestingLevel.get() == 0;
        }
    }
}
