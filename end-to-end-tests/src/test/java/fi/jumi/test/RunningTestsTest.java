// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;
import sample.*;

public class RunningTestsTest {

    private static final int TIMEOUT = 2000;

    @Rule
    public final AppRunner app = new AppRunner();


    @Test(timeout = TIMEOUT)
    public void suite_with_zero_tests() throws Exception {
        app.runTests("sample.notests.NoSuchTest");

        app.checkPassingAndFailingTests(0, 0);
        app.checkTotalTestRuns(0);
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_one_passing_test() throws Exception {
        app.runTests(OnePassingTest.class);

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_one_failing_test() throws Exception {
        app.runTests(OneFailingTest.class);

        app.checkPassingAndFailingTests(1, 1);
        app.checkTotalTestRuns(1);
    }

    @Test(timeout = TIMEOUT)
    public void reports_failure_stack_traces() throws Exception {
        app.runTests(OneFailingTest.class);

        app.checkHasStackTrace(
                "java.lang.AssertionError: dummy failure",
                "at sample.OneFailingTest.testFailing");
    }

    @Test(timeout = TIMEOUT)
    public void tests_are_run_in_parallel() throws Exception {
        app.runTests(ParallelismTest.class);

        app.checkPassingAndFailingTests(3, 0);
        //app.checkTotalTestRuns(2);         // TODO: not implemented
        // TODO: check the test start/end events of each test run, if the test doesn't otherwise fail for lack of RunID
    }

    // TODO: reporting test names
}
