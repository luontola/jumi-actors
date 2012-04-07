// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.*;
import fi.jumi.core.runners.SuiteRunnerIntegrationHelper;
import org.junit.Test;

import java.util.concurrent.Executor;

public class SuiteListenerProtocolTest extends SuiteRunnerIntegrationHelper {

    private static final RunId RUN_1 = new RunId(42);

    @Test
    public void suite_with_zero_test_classes() {
        expect.onSuiteStarted();
        expect.onSuiteFinished();

        runAndCheckExpectations(null);
    }

    @Test
    public void suite_with_one_test_class_with_zero_tests() {
        expect.onSuiteStarted();
        expect.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        expect.onSuiteFinished();

        runAndCheckExpectations(new ZeroTestsDriver(), DummyTest.class);
    }

    @Test
    public void suite_with_one_test_class_with_tests() {
        expect.onSuiteStarted();
        expect.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        expect.onTestStarted(RUN_1, DummyTest.class.getName(), TestId.ROOT);
        expect.onTestFinished(RUN_1, DummyTest.class.getName(), TestId.ROOT);
        expect.onSuiteFinished();

        runAndCheckExpectations(new OneTestDriver(), DummyTest.class);
    }

    @Test
    public void suite_with_failing_tests() {
        expect.onSuiteStarted();
        expect.onTestFound(DummyTest.class.getName(), TestId.ROOT, "DummyTest");
        expect.onTestStarted(RUN_1, DummyTest.class.getName(), TestId.ROOT);
        expect.onFailure(RUN_1, DummyTest.class.getName(), TestId.ROOT, new Exception("dummy failure"));
        expect.onTestFinished(RUN_1, DummyTest.class.getName(), TestId.ROOT);
        expect.onSuiteFinished();

        runAndCheckExpectations(new OneFailingTestDriver(), DummyTest.class);
    }

    // TODO: many runs

    // guinea pigs

    private static class DummyTest {
    }

    public static class ZeroTestsDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
        }
    }

    public static class OneTestDriver implements Driver {
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                public void run() {
                    TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
                    tn.fireTestFinished();
                }
            });
        }
    }

    public static class OneFailingTestDriver implements Driver {
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                public void run() {
                    TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
                    tn.fireFailure(new Exception("dummy failure"));
                    tn.fireTestFinished();
                }
            });
        }
    }
}
