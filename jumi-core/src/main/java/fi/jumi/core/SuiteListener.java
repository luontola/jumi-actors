// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

public interface SuiteListener {

    void onSuiteStarted();

    void onTestFound(String testClass, TestId testId, String name);

    void onRunStarted(RunId runId, String testClass);

    void onTestStarted(RunId runId, TestId testId);

    void onPrintedOut(RunId runId, String text);

    // TODO: onPrintedErr

    void onFailure(RunId runId, Throwable cause);

    void onTestFinished(RunId runId);

    void onRunFinished(RunId runId);

    void onSuiteFinished();
}
