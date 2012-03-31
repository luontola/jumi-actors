// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.core.RunId;
import org.junit.*;
import sample.*;

public class RunningTestsTest {

    private static final int TIMEOUT = 2000;

    @Rule
    public final AppRunner app = new AppRunner();


    @Test(timeout = TIMEOUT)
    public void suite_with_zero_tests() throws Exception {
        app.runTests("sample.notests.NoSuchTest");

        app.checkTotalTests(0);
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_one_passing_test() throws Exception {
        app.runTests(OnePassingTest.class);

        app.checkTotalTests(2);
        app.checkPassingTests(2);
        app.checkFailingTests(0);
        // TODO: check that there was one test run
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_one_failing_test() throws Exception {
        app.runTests(OneFailingTest.class);

        app.checkTotalTests(2);
        app.checkPassingTests(1);
        app.checkFailingTests(1);
        // TODO: check that there was one test run
    }

    @Test(timeout = TIMEOUT)
    public void reports_failure_stack_traces() throws Exception {
        app.runTests(OneFailingTest.class);

        app.checkHasStackTrace(new RunId(42), "java.lang.AssertionError: dummy failure", "at sample.OneFailingTest.testFailing");
    }

    @Test(timeout = TIMEOUT)
    public void tests_are_run_in_parallel() throws Exception {
        app.runTests(ParallelismTest.class);

        app.checkTotalTests(3);
        app.checkPassingTests(3);
        app.checkFailingTests(0);
        // TODO: check that there were two test runs
        // TODO: check the test start/end events of each test run, if the test doesn't otherwise fail for lack of RunID
    }

    // TODO: reporting test names
}
