// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.events.suite.SuiteListenerToEvent;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class TextUI2Test {

    public static final String TEST_CLASS = "com.example.DummyTest";
    public static final String TEST_CLASS_NAME = "DummyTest";
    public static final String ANOTHER_TEST_CLASS = "com.example.AnotherDummyTest";
    public static final String ANOTHER_TEST_CLASS_NAME = "AnotherDummyTest";

    private final FakeStream stream = new FakeStream();
    private final SuiteListener listener = new SuiteListenerToEvent(stream);

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final TextUI2 ui = new TextUI2(new PrintStream(out), new PrintStream(out), stream);

    private String runAndGetOutput() {
        ui.update();
        return out.toString();
    }

    private void firePassingTest(String testClass, TestId rootId, String rootName,
                                 TestId nestedId, String nestedName) {
        // TODO: make this a recursive method? would require and "Object..." parameter
        listener.onTestFound(testClass, rootId, rootName);
        listener.onTestStarted(testClass, rootId);
        firePassingTest(testClass, nestedId, nestedName);
        listener.onTestFinished(testClass, rootId);
    }

    private void firePassingTest(String testClass, TestId id, String name) {
        listener.onTestFound(testClass, id, name);
        listener.onTestStarted(testClass, id);
        listener.onTestFinished(testClass, id);
    }

    private void fireFailingTest(String testClass, TestId id, String name, Throwable cause) {
        listener.onTestFound(testClass, id, name);
        listener.onTestStarted(testClass, id);
        listener.onFailure(testClass, id, cause);
        listener.onTestFinished(testClass, id);
    }

    private void assertInOutput(String expected) {
        assertThat(runAndGetOutput(), containsString(expected));
    }


    private void assertNotInOutput(String expected) {
        assertThat(runAndGetOutput(), not(containsString(expected)));
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
        firePassingTest(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME);
        listener.onSuiteFinished();

        assertInOutput("Pass: 1, Fail: 0, Total: 1");
    }

    @Test
    public void summary_line_for_one_failing_test() {
        listener.onSuiteStarted();
        fireFailingTest(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Throwable("dummy exception"));
        listener.onSuiteFinished();

        assertInOutput("Pass: 0, Fail: 1, Total: 1");
    }

    @Test
    public void summary_line_for_multiple_nested_tests() {
        listener.onSuiteStarted();
        {
            listener.onTestFound(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME);
            listener.onTestStarted(TEST_CLASS, TestId.ROOT);
            // TODO: extract closure?
            firePassingTest(TEST_CLASS, TestId.of(0), "testOne");
            fireFailingTest(TEST_CLASS, TestId.of(1), "testTwo", new Throwable("dummy exception"));
            listener.onTestFinished(TEST_CLASS, TestId.ROOT);
        }
        listener.onSuiteFinished();

        assertInOutput("Pass: 2, Fail: 1, Total: 3");
    }

    @Test
    public void summary_line_is_not_printed_until_all_events_have_arrived() {
        listener.onSuiteStarted();
        assertNotInOutput("Pass");

        listener.onSuiteFinished();
        assertInOutput("Pass");
    }

    @Test
    public void each_TestClass_TestId_pair_is_counted_only_once_in_the_summary() {
        listener.onSuiteStarted();
        firePassingTest(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME,
                TestId.of(0), "test one"
        );
        // same root test is executed twice, but should be counted only once in the total
        firePassingTest(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME,
                TestId.of(1), "test two"
        );
        // a different test class, same TestId, should be counted separately
        firePassingTest(ANOTHER_TEST_CLASS, TestId.ROOT, ANOTHER_TEST_CLASS_NAME);
        listener.onSuiteFinished();

        assertInOutput("Pass: 4, Fail: 0, Total: 4");
    }

    // stack traces

    @Test
    public void prints_failure_stack_traces() {
        listener.onSuiteStarted();
        fireFailingTest(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME, new Throwable("dummy exception"));
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
    public void printes_failure_stack_traces_only_after_the_surrounding_test_is_finished() { // i.e. the test run is finished
        listener.onSuiteStarted();
        {
            {
                listener.onTestFound(TEST_CLASS, TestId.ROOT, TEST_CLASS_NAME);
                listener.onTestStarted(TEST_CLASS, TestId.ROOT);
                fireFailingTest(TEST_CLASS, TestId.of(0), "testOne", new Throwable("dummy exception"));

                assertNotInOutput("java.lang.Throwable: dummy exception");

                listener.onTestFinished(TEST_CLASS, TestId.ROOT);
            }
            assertInOutput("java.lang.Throwable: dummy exception");
        }
        listener.onSuiteFinished();
    }

    // TODO: printing test names
}
