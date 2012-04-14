// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

public interface TestClassRunnerListener {

    // XXX: duplicate method signatures in TestClassListener and TestClassRunnerListener

    void onTestFound(TestId testId, String name);

    void onRunStarted(RunId runId);

    void onTestStarted(RunId runId, TestId testId);

    void onFailure(RunId runId, TestId testId, Throwable cause);

    void onTestFinished(RunId runId, TestId testId);

    void onRunFinished(RunId runId);

    void onTestClassFinished();
}
