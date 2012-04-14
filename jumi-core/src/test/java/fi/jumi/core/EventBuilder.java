// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

import java.util.*;

public class EventBuilder {

    public static final int FIRST_RUN_ID = 1;

    private final SuiteListener listener;

    private final Map<RunId, String> testClassesByRunId = new HashMap<RunId, String>();
    private int nextRunId = FIRST_RUN_ID;

    public EventBuilder(SuiteListener listener) {
        this.listener = listener;
    }

    public RunId nextRunId() {
        try {
            return new RunId(nextRunId);
        } finally {
            nextRunId++;
        }
    }

    public void begin() {
        listener.onSuiteStarted();
    }

    public void end() {
        listener.onSuiteFinished();
    }

    public void runStarted(RunId runId, String testClass) {
        testClassesByRunId.put(runId, testClass);
        listener.onRunStarted(runId, testClass);
    }

    public void runFinished(RunId runId) {
        listener.onRunFinished(runId);
    }

    public void test(RunId runId, TestId testId, String name, Runnable testBody) {
        String testClass = testClassesByRunId.get(runId);
        assert testClass != null;
        listener.onTestFound(testClass, testId, name);

        listener.onTestStarted(runId, testId);
        testBody.run();
        listener.onTestFinished(runId);
    }

    public void test(RunId runId, TestId id, String name) {
        test(runId, id, name, new Runnable() {
            public void run() {
            }
        });
    }

    public void failingTest(final RunId runId, final TestId id, String name, final Throwable failure) {
        test(runId, id, name, new Runnable() {
            public void run() {
                listener.onFailure(runId, failure);
            }
        });
    }
}
