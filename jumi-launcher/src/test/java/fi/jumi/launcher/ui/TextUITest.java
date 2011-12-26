// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteResults;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class TextUITest {

    @Test
    public void shows_number_of_passing_tests() {
        assertThat(outputOf(zeroTests()), containsString("Pass: 0,"));
        assertThat(outputOf(onePassingTest()), containsString("Pass: 1,"));
        assertThat(outputOf(oneFailingTest()), containsString("Pass: 0,"));
        assertThat(outputOf(onePassingAndOneFailingTest()), containsString("Pass: 1,"));
    }

    @Test
    public void shows_number_of_failing_tests() {
        assertThat(outputOf(zeroTests()), containsString("Fail: 0,"));
        assertThat(outputOf(onePassingTest()), containsString("Fail: 0,"));
        assertThat(outputOf(oneFailingTest()), containsString("Fail: 1,"));
        assertThat(outputOf(onePassingAndOneFailingTest()), containsString("Fail: 1,"));
    }

    @Test
    public void shows_total_number_of_tests() {
        assertThat(outputOf(zeroTests()), containsString("Total: 0"));
        assertThat(outputOf(onePassingTest()), containsString("Total: 1"));
        assertThat(outputOf(oneFailingTest()), containsString("Total: 1"));
        assertThat(outputOf(onePassingAndOneFailingTest()), containsString("Total: 2"));
    }

    @Test
    public void shows_failure_stack_traces() {
        assertThat(outputOf(oneFailingTest()), containsString("java.lang.Throwable: dummy failure"));
    }

    // TODO: concurrency


    // helper methods

    private String outputOf(SuiteResults results) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TextUI ui = new TextUI(new PrintStream(out), new PrintStream(out), results);
        ui.runToCompletion();

        return out.toString();
    }

    private static SuiteResults zeroTests() {
        return new SuiteResults()
                .withFinished(true);
    }

    private static SuiteResults onePassingTest() {
        return new SuiteResults()
                .withTest("DummyClass", TestId.ROOT, "testOne")
                .withFinished(true);
    }

    private static SuiteResults oneFailingTest() {
        return new SuiteResults()
                .withTest("DummyClass", TestId.ROOT, "testOne")
                .withFailure("DummyClass", TestId.ROOT, new Throwable("dummy failure"))
                .withFinished(true);
    }

    private static SuiteResults onePassingAndOneFailingTest() {
        return new SuiteResults()
                .withTest("DummyClass", TestId.of(0), "testOne")
                .withTest("DummyClass", TestId.of(1), "testTwo")
                .withFailure("DummyClass", TestId.of(1), new Throwable("dummy failure"))
                .withFinished(true);
    }
}
