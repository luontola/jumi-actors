// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.Rule;
import org.junit.Test;

public class RunningTestsTest {

    private static final int TIMEOUT = 2000;

    @Rule
    public final AppRunner app = new AppRunner();


    @Test(timeout = TIMEOUT)
    public void suite_with_zero_tests() throws Exception {
        app.runTests("sample.notests.*Test");

        app.checkTotalTests(0);
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_one_passing_test() throws Exception {
        app.runTests("sample.OnePassingTest");

        app.checkTotalTests(2);
        app.checkPassingTests(2);
        app.checkFailingTests(0);
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_one_failing_test() throws Exception {
        app.runTests("sample.OneFailingTest");

        app.checkTotalTests(2);
        app.checkPassingTests(1);
        app.checkFailingTests(1);
    }

    // TODO: reporting test names
    // TODO: reporting stack traces
}
