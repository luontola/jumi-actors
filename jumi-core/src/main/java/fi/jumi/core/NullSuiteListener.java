// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.Immutable;

@Immutable
public class NullSuiteListener implements SuiteListener {

    @Override
    public void onSuiteStarted() {
    }

    @Override
    public void onTestFound(String testClass, TestId testId, String name) {
    }

    @Override
    public void onRunStarted(RunId runId, String testClass) {
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
    }

    @Override
    public void onPrintedOut(RunId runId, String text) {
    }

    @Override
    public void onFailure(RunId runId, Throwable cause) {
    }

    @Override
    public void onTestFinished(RunId runId) {
    }

    @Override
    public void onRunFinished(RunId runId) {
    }

    @Override
    public void onSuiteFinished() {
    }
}
