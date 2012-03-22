// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;

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

        int run1 = suite.nextRunId();
        suite.test(run1, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME);

        suite.end();
    }

    public static void oneFailingTest(SuiteListener listener) {
        EventBuilder suite = new EventBuilder(listener);
        suite.begin();

        int run1 = suite.nextRunId();
        suite.failingTest(run1, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME,
                new Throwable("dummy exception")
        );

        suite.end();
    }

    public static void nestedFailingAndPassingTests(SuiteListener listener) {
        final EventBuilder suite = new EventBuilder(listener);
        suite.begin();

        final int run1 = suite.nextRunId();
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

        final int run1 = suite.nextRunId();
        suite.test(run1, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Runnable() {
            public void run() {
                suite.test(run1, TEST_CLASS, TestId.of(0), "testOne");
            }
        });

        final int run2 = suite.nextRunId();
        suite.test(run2, TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Runnable() {
            public void run() {
                suite.test(run2, TEST_CLASS, TestId.of(1), "testTwo");
            }
        });

        suite.end();
    }
}
