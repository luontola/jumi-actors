// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public abstract class TestRunListener implements SuiteListener {

    private static void assertShouldNotBeCalled() {
        throw new AssertionError("should not be called; not a run-specific event");
    }

    @Override
    public final void onSuiteStarted() {
        assertShouldNotBeCalled();
    }

    @Override
    public final void onSuiteFinished() {
        assertShouldNotBeCalled();
    }

    @Override
    public final void onTestFound(String testClass, TestId testId, String name) {
        assertShouldNotBeCalled();
    }
}
