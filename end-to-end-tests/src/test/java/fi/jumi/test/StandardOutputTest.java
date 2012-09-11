// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.core.runs.RunId;
import org.junit.*;
import sample.PrintingTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class StandardOutputTest {

    @Rule
    public final AppRunner app = new AppRunner();


    @Ignore("not implemented") // TODO
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void shows_what_tests_print_to_stdout() throws Exception {
        assertThat(outputOf(PrintingTest.class, "testPrintOut"), containsString("printed to stdout"));
    }

    @Ignore("not implemented") // TODO
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void shows_what_tests_print_to_stderr() throws Exception {
        assertThat(outputOf(PrintingTest.class, "testPrintErr"), containsString("printed to stderr"));
    }

    @Ignore("not implemented") // TODO
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void printing_to_stdout_and_stderr_is_synchronous() throws Exception {
        assertThat(outputOf(PrintingTest.class, "testInterleavedPrinting"), containsString("trololo"));
    }

    private String outputOf(Class<?> testClass, String testName) throws Exception {
        app.runTests(testClass);
        RunId runId = app.findRun(testClass.getSimpleName(), testName, "/", "/");
        return app.getRunOutput(runId);
    }
}
