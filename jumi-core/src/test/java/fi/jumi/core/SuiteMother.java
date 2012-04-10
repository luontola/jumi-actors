// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;

public class SuiteMother {

    public static final String TEST_CLASS = "com.example.DummyTest";
    public static final String TEST_CLASS_NAME = "DummyTest";

    public static void emptySuite(SuiteListener listener) {
        EventBuilder suite = new EventBuilder(listener);
        suite.begin();
        suite.end();
    }

    public static void onePassingTest(SuiteListener listener) {
        EventBuilder suite = new EventBuilder(listener);
        suite.begin();

        RunId run1 = suite.nextRunId();
        suite.test(run1, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME);

        suite.end();
    }

    public static void oneFailingTest(SuiteListener listener) {
        EventBuilder suite = new EventBuilder(listener);
        suite.begin();

        RunId run1 = suite.nextRunId();
        suite.failingTest(run1, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME,
                new Throwable("dummy exception")
        );

        suite.end();
    }

    public static void nestedFailingAndPassingTests(SuiteListener listener) {
        final EventBuilder suite = new EventBuilder(listener);
        suite.begin();

        final RunId run1 = suite.nextRunId();
        suite.test(run1, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Runnable() {
            public void run() {
                suite.test(run1, TEST_CLASS, TestId.of(0), "testOne");
                suite.failingTest(run1, TEST_CLASS, TestId.of(1), "testTwo",
                        new Throwable("dummy exception")
                );
            }
        });

        suite.end();
    }

    public static void twoPassingRuns(SuiteListener listener) {
        final EventBuilder suite = new EventBuilder(listener);
        suite.begin();

        final RunId run1 = suite.nextRunId();
        suite.test(run1, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Runnable() {
            public void run() {
                suite.test(run1, TEST_CLASS, TestId.of(0), "testOne");
            }
        });

        final RunId run2 = suite.nextRunId();
        suite.test(run2, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Runnable() {
            public void run() {
                suite.test(run2, TEST_CLASS, TestId.of(1), "testTwo");
            }
        });

        suite.end();
    }

    public static void twoInterleavedRuns(final SuiteListener listener) {
        final EventBuilder suite = new EventBuilder(listener);
        suite.begin();

        final RunId run1 = suite.nextRunId();
        final RunId run2 = suite.nextRunId();
        listener.onTestFound(TEST_CLASS, TestId.of(0), "testOne");
        listener.onTestFound(TEST_CLASS, TestId.of(1), "testTwo");
        listener.onTestStarted(run1, TEST_CLASS, TestId.of(0));
        listener.onTestStarted(run2, TEST_CLASS, TestId.of(1));
        listener.onTestFinished(run1, TEST_CLASS, TestId.of(0));
        listener.onTestFinished(run2, TEST_CLASS, TestId.of(1));

        suite.end();
    }
}
