// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.core.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class CurrentRun {

    // FIXME: not yet thread-safe

    private final RunIdSequence runIdSequence;
    private RunId currentRun = null;
    private int testNestingLevel = 0;

    public CurrentRun(RunIdSequence runIdSequence) {
        this.runIdSequence = runIdSequence;
    }

    public void enterTest() {
        assert testNestingLevel >= 0;

        if (testNestingLevel == 0) {
            currentRun = runIdSequence.nextRunId();
        }
        testNestingLevel++;
    }

    public RunId getRunId() {
        assert currentRun != null;

        return currentRun;
    }

    public void exitTest() {
        assert testNestingLevel >= 0;

        testNestingLevel--;
        if (testNestingLevel == 0) {
            currentRun = null;
        }
    }
}
