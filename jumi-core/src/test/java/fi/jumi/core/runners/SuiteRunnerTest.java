// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;
import fi.jumi.core.RunId;
import org.junit.Test;

import java.util.concurrent.Executor;

public class SuiteRunnerTest extends SuiteRunnerIntegrationHelper {

    public static final String TEST_CLASS_1 = DummyTest.class.getName();
    public static final String TEST_CLASS_2 = SecondDummyTest.class.getName();
    // TODO: launches every testclass found, using its driver

    /**
     * Responsibility delegated to {@link fi.jumi.core.runners.DuplicateOnTestFoundEventFilter}
     */
    @Test
    public void filters_duplicate_onTestFound_events() {
        expect.onSuiteStarted();
        expect.onTestFound(DummyTest.class.getName(), TestId.ROOT, "fireTestFound called twice");
        expect.onSuiteFinished();

        runAndCheckExpectations(new DuplicateFireTestFoundDriver(), DummyTest.class);
    }

    @Test
    public void notifies_when_all_test_classes_are_finished() {
        // TODO: these expectations are not interesting for this test - find a way to write this test without mentioning them
        expect.onSuiteStarted();
        expect.onTestFound(TEST_CLASS_1, TestId.ROOT, "DummyTest");
        expect.onTestStarted(new RunId(42), TEST_CLASS_1, TestId.ROOT);
        expect.onTestFinished(new RunId(42), TEST_CLASS_1, TestId.ROOT);
        expect.onTestFound(TEST_CLASS_2, TestId.ROOT, "SecondDummyTest");
        expect.onTestStarted(new RunId(42), TEST_CLASS_2, TestId.ROOT);
        expect.onTestFinished(new RunId(42), TEST_CLASS_2, TestId.ROOT);

        // this must happen last, once
        expect.onSuiteFinished();

        runAndCheckExpectations(new TestClassWithZeroTestsDriver(), DummyTest.class, SecondDummyTest.class);
    }


    // guinea pigs

    private static class DummyTest {
    }

    private static class SecondDummyTest {
    }

    public static class DuplicateFireTestFoundDriver implements Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "fireTestFound called twice");
            notifier.fireTestFound(TestId.ROOT, "fireTestFound called twice");
        }
    }

    public static class TestClassWithZeroTestsDriver implements Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            notifier.fireTestStarted(TestId.ROOT)
                    .fireTestFinished();
        }
    }
}
