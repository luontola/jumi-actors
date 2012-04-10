// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.core.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class CurrentRun {

    private final RunIdSequence runIdSequence;

    private final InheritableThreadLocal<RunContext> currentRunContext = new InheritableThreadLocal<RunContext>() {
        @Override
        protected RunContext initialValue() {
            return new RunContext();
        }
    };

    public CurrentRun(RunIdSequence runIdSequence) {
        this.runIdSequence = runIdSequence;
    }

    public void enterTest() {
        RunContext context = this.currentRunContext.get();
        assert context.testNestingLevel >= 0;

        if (context.testNestingLevel == 0) {
            context.currentRun = runIdSequence.nextRunId();
        }
        context.testNestingLevel++;
    }

    public RunId getRunId() {
        RunContext context = this.currentRunContext.get();
        assert context.currentRun != null;

        return context.currentRun;
    }

    public void exitTest() {
        RunContext context = this.currentRunContext.get();
        assert context.testNestingLevel >= 0;

        context.testNestingLevel--;
        if (context.testNestingLevel == 0) {
            context.currentRun = null;
        }
    }

    private static class RunContext {
        private RunId currentRun = null;
        private int testNestingLevel = 0;
    }
}
