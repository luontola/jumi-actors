// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.Event;
import fi.jumi.actors.MessageQueue;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.events.suite.SuiteListenerToEvent;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static fi.jumi.core.utils.Asserts.assertContainsSubStrings;
import static fi.jumi.core.utils.Asserts.assertNotContainsSubStrings;

public class TextUITest {

    private static final String TEST_CLASS = "com.example.DummyTest";
    private static final String TEST_CLASS_NAME = "DummyTest";
    private static final String ANOTHER_TEST_CLASS = "com.example.AnotherDummyTest";
    private static final String ANOTHER_TEST_CLASS_NAME = "AnotherDummyTest";

    private static final String SUMMARY_LINE = "Pass";

    private final MessageQueue<Event<SuiteListener>> stream = new MessageQueue<Event<SuiteListener>>();
    private final SuiteListener listener = new SuiteListenerToEvent(stream);

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final TextUI ui = new TextUI(new PrintStream(out), new PrintStream(out), stream);

    private String runAndGetOutput() {
        ui.update();
        return out.toString();
    }

    private void test(String testClass, TestId id, String name, Runnable testBody) {
        listener.onTestFound(testClass, id, name);
        listener.onTestStarted(testClass, id);
        testBody.run();
        listener.onTestFinished(testClass, id);
    }

    private void test(String testClass, TestId id, String name) {
        test(testClass, id, name, new Runnable() {
            public void run() {
            }
        });
    }

    private void failingTest(final String testClass, final TestId id, String name, final Throwable failure) {
        test(testClass, id, name, new Runnable() {
            public void run() {
                listener.onFailure(testClass, id, failure);
            }
        });
    }

    private void assertInOutput(String... expectedLines) {
        assertContainsSubStrings(runAndGetOutput(), expectedLines);
    }

    private void assertNotInOutput(String... expectedLines) {
        assertNotContainsSubStrings(runAndGetOutput(), expectedLines);
    }

    // updating

    @Test(timeout = 1000L)
    public void can_update_non_blockingly() {
        ui.update(); // given no events in stream, should exit quickly

        assertNotInOutput(SUMMARY_LINE);
    }

    @Test(timeout = 1000L)
    public void can_update_blockingly() throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            public void run() {
                listener.onSuiteStarted();
                listener.onSuiteFinished();
            }
        });
        t.start();

        ui.updateUntilFinished(); // should exit only after all events have arrived

        assertInOutput(SUMMARY_LINE);
    }

    // summary line

    @Test
    public void summary_line_for_no_tests() {
        listener.onSuiteStarted();
        listener.onSuiteFinished();

        assertInOutput("Pass: 0, Fail: 0, Total: 0");
    }

    @Test
    public void summary_line_for_one_passing_test() {
        listener.onSuiteStarted();
        test(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME);
        listener.onSuiteFinished();

        assertInOutput("Pass: 1, Fail: 0, Total: 1");
    }

    @Test
    public void summary_line_for_one_failing_test() {
        listener.onSuiteStarted();
        failingTest(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME,
                new Throwable("dummy exception")
        );
        listener.onSuiteFinished();

        assertInOutput("Pass: 0, Fail: 1, Total: 1");
    }

    @Test
    public void summary_line_for_multiple_nested_tests() {
        listener.onSuiteStarted();
        test(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Runnable() {
            public void run() {
                test(TEST_CLASS, TestId.of(0), "testOne");
                failingTest(TEST_CLASS, TestId.of(1), "testTwo",
                        new Throwable("dummy exception")
                );
            }
        });
        listener.onSuiteFinished();

        assertInOutput("Pass: 2, Fail: 1, Total: 3");
    }

    @Test
    public void summary_line_is_not_printed_until_all_events_have_arrived() {
        listener.onSuiteStarted();
        assertNotInOutput(SUMMARY_LINE);

        listener.onSuiteFinished();
        assertInOutput(SUMMARY_LINE);
    }

    @Test
    public void each_TestClass_TestId_pair_is_counted_only_once_in_the_summary() {
        listener.onSuiteStarted();
        test(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Runnable() {
            public void run() {
                test(TEST_CLASS, TestId.of(0), "test one");
            }
        });
        // same root test is executed twice, but should be counted only once in the total
        test(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Runnable() {
            public void run() {
                test(TEST_CLASS, TestId.of(1), "test two");
            }
        });
        // a different test class, same TestId, should be counted separately
        test(ANOTHER_TEST_CLASS, TestId.ROOT, ANOTHER_TEST_CLASS_NAME);
        listener.onSuiteFinished();

        assertInOutput("Pass: 4, Fail: 0, Total: 4");
    }

    // test names

    @Test
    public void prints_test_run_header() {
        listener.onSuiteStarted();
        test("com.example.DummyTest", TestId.ROOT, "Dummy test");
        listener.onSuiteFinished();

        assertInOutput(
                "Run #42 in com.example.DummyTest",
                "Dummy test"
        );
    }

    @Test
    public void test_run_header_is_printed_only_once_per_test_run() {
        listener.onSuiteStarted();
        test(TEST_CLASS, TestId.ROOT, "Dummy test", new Runnable() {
            public void run() {
                test(TEST_CLASS, TestId.of(0), "test one");
            }
        });
        listener.onSuiteFinished();

        assertInOutput(TEST_CLASS);
        assertNotInOutput(TEST_CLASS, TEST_CLASS);
    }

    @Test
    public void prints_when_a_test_starts_and_ends() {
        listener.onSuiteStarted();
        test("com.example.DummyTest", TestId.ROOT, "Dummy test");
        listener.onSuiteFinished();

        assertInOutput(
                "+ Dummy test",
                "- Dummy test"
        );
    }

    @Test
    public void prints_with_indentation_when_a_nested_test_starts_and_ends() {
        listener.onSuiteStarted();
        test(TEST_CLASS, TestId.ROOT, "Dummy test", new Runnable() {
            public void run() {
                test(TEST_CLASS, TestId.of(0), "test one");
                test(TEST_CLASS, TestId.of(1), "test two", new Runnable() {
                    public void run() {
                        test(TEST_CLASS, TestId.of(1, 0), "deeply nested test");
                    }
                });
            }
        });
        listener.onSuiteFinished();

        assertInOutput(
                " + Dummy test",
                "   + test one",
                "   - test one",
                "   + test two",
                "     + deeply nested test",
                "     - deeply nested test",
                "   - test two",
                " - Dummy test"
        );
    }

    // stack traces

    @Test
    public void prints_failure_stack_traces() {
        listener.onSuiteStarted();
        failingTest(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME,
                new Throwable("dummy exception")
        );
        listener.onSuiteFinished();

        assertInOutput("java.lang.Throwable: dummy exception");
    }

    @Test
    public void prints_failure_stack_traces_only_after_the_test_is_finished() {
        listener.onSuiteStarted();
        {
            {
                listener.onTestFound(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME);
                listener.onTestStarted(TEST_CLASS, TestId.ROOT);
                listener.onFailure(TEST_CLASS, TestId.ROOT, new Throwable("dummy exception"));

                assertNotInOutput("java.lang.Throwable: dummy exception");

                listener.onTestFinished(TEST_CLASS, TestId.ROOT);
            }

            assertInOutput("java.lang.Throwable: dummy exception");
        }
        listener.onSuiteFinished();
    }

    @Test
    public void prints_failure_stack_traces_only_after_the_surrounding_test_is_finished() { // i.e. the test run is finished
        listener.onSuiteStarted();
        {
            {
                listener.onTestFound(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME);
                listener.onTestStarted(TEST_CLASS, TestId.ROOT);
                failingTest(TEST_CLASS, TestId.of(0), "testOne",
                        new Throwable("dummy exception")
                );

                assertNotInOutput("java.lang.Throwable: dummy exception");

                listener.onTestFinished(TEST_CLASS, TestId.ROOT);
            }
            assertInOutput("java.lang.Throwable: dummy exception");
        }
        listener.onSuiteFinished();
    }
}
