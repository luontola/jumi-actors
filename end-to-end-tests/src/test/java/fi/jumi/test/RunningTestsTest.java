// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiLauncher;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RunningTestsTest {

    private static final int TIMEOUT = 2000;

    @Rule public final AppRunner app = new AppRunner();
    private final JumiLauncher launcher = app.launcher;

    @Test(timeout = TIMEOUT)
    public void suite_with_zero_tests() throws Exception {
        launcher.addToClassPath(TestEnvironment.getSampleClasses());
        launcher.setTestsToInclude("sample.notests.*Test");
        launcher.start();
        launcher.awaitSuiteFinished();

        assertThat("total tests", launcher.getTotalTests(), is(0));
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_one_test() throws Exception {
        launcher.addToClassPath(TestEnvironment.getSampleClasses());
        launcher.setTestsToInclude("sample.OnePassingTest");
        launcher.start();
        launcher.awaitSuiteFinished();

        assertThat("total tests", launcher.getTotalTests(), is(2)); // test class plus its one test method
    }

    // TODO: passing & failing tests
    // TODO: reporting test names
    // TODO: reporting stack traces
}
