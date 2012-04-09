// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.RunId;

public interface TestClassListener {

    // XXX: duplicate method signatures in TestClassListener and TestClassRunnerListener

    void onTestFound(TestId id, String name);

    void onTestStarted(RunId runId, TestId id);

    void onFailure(TestId id, Throwable cause);

    void onTestFinished(TestId id);
}
