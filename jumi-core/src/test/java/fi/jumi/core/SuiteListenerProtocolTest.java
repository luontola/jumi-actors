// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.*;
import fi.jumi.core.runners.SuiteRunnerIntegrationHelper;
import fi.jumi.core.runs.RunId;
import org.junit.Test;

import java.util.concurrent.Executor;

public class SuiteListenerProtocolTest extends SuiteRunnerIntegrationHelper {

    private static final RunId RUN_1 = new RunId(1);
    private static final RunId RUN_2 = new RunId(2);
    private static final Class<?> CLASS_1 = DummyTest.class;
    private static final Class<?> CLASS_2 = SecondDummyTest.class;

    @Test
    public void suite_with_zero_test_classes() {
        expect.onSuiteStarted();
        expect.onSuiteFinished();

        runAndCheckExpectations(null);
    }

    @Test
    public void suite_with_one_test_class_with_zero_tests() {
        expect.onSuiteStarted();
        expect.onTestFound(CLASS_1.getName(), TestId.ROOT, "DummyTest");
        expect.onSuiteFinished();

        runAndCheckExpectations(new ZeroTestsDriver(), CLASS_1);
    }

    @Test
    public void suite_with_one_test_class_with_tests() {
        expect.onSuiteStarted();
        expect.onTestFound(CLASS_1.getName(), TestId.ROOT, "DummyTest");
        expect.onRunStarted(RUN_1, CLASS_1.getName());
        expect.onTestStarted(RUN_1, CLASS_1.getName(), TestId.ROOT);
        expect.onTestFinished(RUN_1, CLASS_1.getName(), TestId.ROOT);
        expect.onRunFinished(RUN_1);
        expect.onSuiteFinished();

        runAndCheckExpectations(new OneTestDriver(), CLASS_1);
    }

    @Test
    public void suite_with_failing_tests() {
        expect.onSuiteStarted();
        expect.onTestFound(CLASS_1.getName(), TestId.ROOT, "DummyTest");
        expect.onRunStarted(RUN_1, CLASS_1.getName());
        expect.onTestStarted(RUN_1, CLASS_1.getName(), TestId.ROOT);
        expect.onFailure(RUN_1, CLASS_1.getName(), TestId.ROOT, new Exception("dummy failure"));
        expect.onTestFinished(RUN_1, CLASS_1.getName(), TestId.ROOT);
        expect.onRunFinished(RUN_1);
        expect.onSuiteFinished();

        runAndCheckExpectations(new OneFailingTestDriver(), CLASS_1);
    }

    @Test
    public void suite_with_nested_tests() {
        expect.onSuiteStarted();
        expect.onTestFound(CLASS_1.getName(), TestId.ROOT, "DummyTest");

        expect.onTestFound(CLASS_1.getName(), TestId.of(0), "parent test");
        expect.onRunStarted(RUN_1, CLASS_1.getName());
        expect.onTestStarted(RUN_1, CLASS_1.getName(), TestId.of(0));
        {
            expect.onTestFound(CLASS_1.getName(), TestId.of(0, 0), "child test");
            expect.onTestStarted(RUN_1, CLASS_1.getName(), TestId.of(0, 0));
            expect.onTestFinished(RUN_1, CLASS_1.getName(), TestId.of(0, 0));
        }
        expect.onTestFinished(RUN_1, CLASS_1.getName(), TestId.of(0));
        expect.onRunFinished(RUN_1);

        expect.onSuiteFinished();

        runAndCheckExpectations(new NestedTestsDriver(), CLASS_1);
    }

    @Test
    public void suite_with_many_runs() {
        expect.onSuiteStarted();
        expect.onTestFound(CLASS_1.getName(), TestId.ROOT, "DummyTest");

        expect.onTestFound(CLASS_1.getName(), TestId.of(0), "test one");
        expect.onRunStarted(RUN_1, CLASS_1.getName());
        expect.onTestStarted(RUN_1, CLASS_1.getName(), TestId.of(0));
        expect.onTestFinished(RUN_1, CLASS_1.getName(), TestId.of(0));
        expect.onRunFinished(RUN_1);

        expect.onTestFound(CLASS_1.getName(), TestId.of(1), "test two");
        expect.onRunStarted(RUN_2, CLASS_1.getName());
        expect.onTestStarted(RUN_2, CLASS_1.getName(), TestId.of(1));
        expect.onTestFinished(RUN_2, CLASS_1.getName(), TestId.of(1));
        expect.onRunFinished(RUN_2);

        expect.onSuiteFinished();

        runAndCheckExpectations(new ManyTestRunsDriver(), CLASS_1);
    }

    @Test
    public void suite_with_many_test_classes() {
        expect.onSuiteStarted();

        expect.onTestFound(CLASS_1.getName(), TestId.ROOT, "DummyTest");
        expect.onTestFound(CLASS_2.getName(), TestId.ROOT, "SecondDummyTest");

        expect.onRunStarted(new RunId(1), CLASS_1.getName());
        expect.onTestStarted(new RunId(1), CLASS_1.getName(), TestId.ROOT);
        expect.onTestFinished(new RunId(1), CLASS_1.getName(), TestId.ROOT);
        expect.onRunFinished(new RunId(1));

        expect.onRunStarted(new RunId(2), CLASS_2.getName());
        expect.onTestStarted(new RunId(2), CLASS_2.getName(), TestId.ROOT);
        expect.onTestFinished(new RunId(2), CLASS_2.getName(), TestId.ROOT);
        expect.onRunFinished(new RunId(2));

        expect.onSuiteFinished();

        runAndCheckExpectations(new OneTestDriver(), CLASS_1, CLASS_2);
    }


    // guinea pigs

    private static class DummyTest {
    }

    private static class SecondDummyTest {
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

    public static class NestedTestsDriver implements Driver {
        public void findTests(final Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                public void run() {
                    notifier.fireTestFound(TestId.of(0), "parent test");
                    TestNotifier parent = notifier.fireTestStarted(TestId.of(0));

                    notifier.fireTestFound(TestId.of(0, 0), "child test");
                    TestNotifier child = notifier.fireTestStarted(TestId.of(0, 0));

                    child.fireTestFinished();
                    parent.fireTestFinished();
                }
            });
        }
    }

    public static class ManyTestRunsDriver implements Driver {
        public void findTests(final Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                public void run() {
                    notifier.fireTestFound(TestId.of(0), "test one");
                    notifier.fireTestStarted(TestId.of(0))
                            .fireTestFinished();
                }
            });
            executor.execute(new Runnable() {
                public void run() {
                    notifier.fireTestFound(TestId.of(1), "test two");
                    notifier.fireTestStarted(TestId.of(1))
                            .fireTestFinished();
                }
            });
        }
    }
}
