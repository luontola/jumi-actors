// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

import java.util.*;

public abstract class DenormalizedRunVisitor extends RunVisitor {

    private String testClass;
    private final Deque<TestId> runningTests = new ArrayDeque<TestId>();

    public String getTestClass() {
        return testClass;
    }

    public TestId getTestId() {
        return runningTests.getFirst();
    }

    public int getTestNestingLevel() {
        return runningTests.size();
    }

    @Override
    public void onRunStarted(RunId runId, String testClass) {
        this.testClass = testClass;
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        runningTests.push(testId);
    }

    @Override
    public void onFailure(RunId runId, Throwable cause) {
    }

    @Override
    public void onTestFinished(RunId runId) {
        runningTests.pop(); // XXX: this line is not tested
    }

    @Override
    public void onRunFinished(RunId runId) {
        this.testClass = null;
    }
}
