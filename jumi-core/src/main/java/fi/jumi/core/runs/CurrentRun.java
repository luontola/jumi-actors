// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
public class CurrentRun {

    private final RunIdSequence runIdSequence;
    private final InheritableThreadLocal<RunContext> currentRun = new InheritableThreadLocal<RunContext>();

    public CurrentRun(RunIdSequence runIdSequence) {
        this.runIdSequence = runIdSequence;
    }

    public boolean enterTest() {
        boolean newRun = false;
        RunContext context = currentRun.get();
        if (context == null) {
            context = new RunContext(runIdSequence.nextRunId());
            currentRun.set(context);
            newRun = true;
        }
        context.enterTest();
        return newRun;
    }

    public RunId getRunId() {
        RunContext context = currentRun.get();
        return context.runId;
    }

    public boolean exitTest() {
        RunContext context = currentRun.get();
        context.exitTest();
        if (context.exitedAllTests()) {
            currentRun.remove();
            return true;
        }
        return false;
    }


    @ThreadSafe
    private static class RunContext {
        public final RunId runId;
        private final AtomicInteger testNestingLevel = new AtomicInteger(0);

        public RunContext(RunId runId) {
            this.runId = runId;
        }

        public void enterTest() {
            testNestingLevel.incrementAndGet();
        }

        public void exitTest() {
            int level = testNestingLevel.decrementAndGet();
            assert level >= 0;
        }

        public boolean exitedAllTests() {
            return testNestingLevel.get() == 0;
        }
    }
}
