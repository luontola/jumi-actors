// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;

public class EventBuilder {

    public static final int FIRST_RUN_ID = 1;

    private final SuiteListener listener;

    private int nextRunId = FIRST_RUN_ID;

    public EventBuilder(SuiteListener listener) {
        this.listener = listener;
    }

    public int nextRunId() {
        try {
            return nextRunId;
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

    public void test(int runId, String testClass, TestId id, String name, Runnable testBody) {
        listener.onTestFound(testClass, id, name);
        listener.onTestStarted(runId, testClass, id);
        testBody.run();
        listener.onTestFinished(runId, testClass, id);
    }

    public void test(int runId, String testClass, TestId id, String name) {
        test(runId, testClass, id, name, new Runnable() {
            public void run() {
            }
        });
    }

    public void failingTest(final int runId, final String testClass, final TestId id, String name, final Throwable failure) {
        test(runId, testClass, id, name, new Runnable() {
            public void run() {
                listener.onFailure(runId, testClass, id, failure);
            }
        });
    }
}
