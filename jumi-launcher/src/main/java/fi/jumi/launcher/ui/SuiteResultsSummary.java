// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

import java.util.*;

public class SuiteResultsSummary implements RunVisitor {

    private final Set<GlobalTestId> failedTests = new HashSet<GlobalTestId>();
    private final Set<GlobalTestId> allTests = new HashSet<GlobalTestId>();

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

    public int getPassingTests() {
        int totalCount = getTotalTests();
        int failCount = getFailingTests();
        return totalCount - failCount;
    }

    public int getFailingTests() {
        return failedTests.size();
    }

    public int getTotalTests() {
        return allTests.size();
    }
}
