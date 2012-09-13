// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

public interface RunVisitor {

    void onRunStarted(RunId runId, String testClass);

    void onTestStarted(RunId runId, String testClass, TestId testId);

    void onPrintedOut(RunId runId, String testClass, TestId testId, String text);

    void onPrintedErr(RunId runId, String testClass, TestId testId, String text);

    void onFailure(RunId runId, String testClass, TestId testId, Throwable cause);

    void onTestFinished(RunId runId, String testClass, TestId testId);

    void onRunFinished(RunId runId, String testClass);
}
