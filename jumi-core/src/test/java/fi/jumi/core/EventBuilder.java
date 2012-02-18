// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;

public class EventBuilder {

    private final SuiteListener listener;

    public EventBuilder(SuiteListener listener) {
        this.listener = listener;
    }

    public void begin() {
        listener.onSuiteStarted();
    }

    public void end() {
        listener.onSuiteFinished();
    }

    public void test(String testClass, TestId id, String name, Runnable testBody) {
        listener.onTestFound(testClass, id, name);
        listener.onTestStarted(42, testClass, id);
        testBody.run();
        listener.onTestFinished(42, testClass, id);
    }

    public void test(String testClass, TestId id, String name) {
        test(testClass, id, name, new Runnable() {
            public void run() {
            }
        });
    }

    public void failingTest(final String testClass, final TestId id, String name, final Throwable failure) {
        test(testClass, id, name, new Runnable() {
            public void run() {
                listener.onFailure(42, testClass, id, failure);
            }
        });
    }
}
