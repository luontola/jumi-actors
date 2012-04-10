// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

public interface SuiteListener {

    void onSuiteStarted();

    void onSuiteFinished();

    void onTestFound(String testClass, TestId testId, String name);

    // TODO: add onRunStarted, onRunFinished
    // TODO: remove testClass when it can be deduced from runId

    void onTestStarted(RunId runId, String testClass, TestId testId);

    void onTestFinished(RunId runId, String testClass, TestId testId);

    void onFailure(RunId runId, String testClass, TestId testId, Throwable cause);
}
