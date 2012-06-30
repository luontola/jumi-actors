// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class RunEventDenormalizer implements SuiteListener {

    private final RunVisitor visitor;
    private final Deque<TestId> runningTests = new ArrayDeque<TestId>();
    private String testClass;

    public RunEventDenormalizer(RunVisitor visitor) {
        this.visitor = visitor;
    }

    private TestId getTestId() {
        return runningTests.getFirst();
    }

    @Override
    public final void onSuiteStarted() {
        assertShouldNotBeCalled();
    }

    @Override
    public final void onTestFound(String testClass, TestId testId, String name) {
        assertShouldNotBeCalled();
    }

    @Override
    public void onRunStarted(RunId runId, String testClass) {
        this.testClass = testClass;
        visitor.onRunStarted(runId, testClass);
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        runningTests.push(testId);
        visitor.onTestStarted(runId, testClass, testId);
    }

    @Override
    public void onFailure(RunId runId, Throwable cause) {
        visitor.onFailure(runId, testClass, getTestId(), cause);
    }

    @Override
    public void onTestFinished(RunId runId) {
        visitor.onTestFinished(runId, testClass, getTestId());
        runningTests.pop();
    }

    @Override
    public void onRunFinished(RunId runId) {
        visitor.onRunFinished(runId, testClass);
        this.testClass = null;
    }

    @Override
    public final void onSuiteFinished() {
        assertShouldNotBeCalled();
    }

    private static void assertShouldNotBeCalled() {
        throw new AssertionError("should not be called; not a run-specific event");
    }
}
