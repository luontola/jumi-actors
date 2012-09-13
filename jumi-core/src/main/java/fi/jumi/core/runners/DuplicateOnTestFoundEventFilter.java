// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
class DuplicateOnTestFoundEventFilter implements TestClassListener {

    private final TestClassListener target;
    private final Map<TestId, String> tests = new HashMap<>();

    public DuplicateOnTestFoundEventFilter(TestClassListener target) {
        this.target = target;
    }

    @Override
    public void onTestFound(TestId testId, String name) {
        if (hasNotBeenFoundBefore(testId)) {
            checkParentWasFoundFirst(testId);
            rememberFoundTest(testId, name);
            target.onTestFound(testId, name);
        } else {
            checkNameIsSameAsBefore(testId, name);
        }
    }

    private void rememberFoundTest(TestId testId, String name) {
        tests.put(testId, name);
    }

    private boolean hasNotBeenFoundBefore(TestId testId) {
        return !tests.containsKey(testId);
    }

    private void checkParentWasFoundFirst(TestId testId) {
        if (!testId.isRoot() && hasNotBeenFoundBefore(testId.getParent())) {
            throw new IllegalStateException("parent of " + testId + " must be found first");
        }
    }

    private void checkNameIsSameAsBefore(TestId testId, String newName) {
        String oldName = tests.get(testId);
        if (oldName != null && !oldName.equals(newName)) {
            throw new IllegalArgumentException("test " + testId + " was already found with another name: " + oldName);
        }
    }

    // events which are delegated as-is

    @Override
    public void onRunStarted(RunId runId) {
        target.onRunStarted(runId);
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        target.onTestStarted(runId, testId);
    }

    @Override
    public void onPrintedOut(RunId runId, String text) {
        target.onPrintedOut(runId, text);
    }

    @Override
    public void onPrintedErr(RunId runId, String text) {
        target.onPrintedErr(runId, text);
    }

    @Override
    public void onFailure(RunId runId, TestId testId, Throwable cause) {
        target.onFailure(runId, testId, cause);
    }

    @Override
    public void onTestFinished(RunId runId, TestId testId) {
        target.onTestFinished(runId, testId);
    }

    @Override
    public void onRunFinished(RunId runId) {
        target.onRunFinished(runId);
    }
}
