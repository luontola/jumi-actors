// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class SuiteResultsSummary implements RunVisitor {

    private final Set<GlobalTestId> failedTests = new HashSet<>();
    private final Set<GlobalTestId> allTests = new HashSet<>();

    public int getPassingTests() {
        return getTotalTests() - getFailingTests();
    }

    public int getFailingTests() {
        return failedTests.size();
    }

    public int getTotalTests() {
        return allTests.size();
    }

    @Override
    public void onRunStarted(RunId runId, String testClass) {
    }

    @Override
    public void onTestStarted(RunId runId, String testClass, TestId testId) {
        allTests.add(new GlobalTestId(testClass, testId));
    }

    @Override
    public void onFailure(RunId runId, String testClass, TestId testId, Throwable cause) {
        failedTests.add(new GlobalTestId(testClass, testId));
    }

    @Override
    public void onTestFinished(RunId runId, String testClass, TestId testId) {
    }

    @Override
    public void onRunFinished(RunId runId, String testClass) {
    }
}
