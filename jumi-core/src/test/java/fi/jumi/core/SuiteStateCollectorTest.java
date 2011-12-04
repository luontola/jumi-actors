// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SuiteStateCollectorTest {

    @Test
    public void suite_with_zero_tests() {
        SuiteStateCollector listener = new SuiteStateCollector();
        listener.onSuiteStarted();
        listener.onSuiteFinished();

        SuiteResults results = listener.getState();
        assertThat("total", results.getTotalTests(), is(0));
        assertThat("passing", results.getPassingTests(), is(0));
        assertThat("failing", results.getFailingTests(), is(0));
    }

    @Test
    public void suite_with_one_passing_test() {
        SuiteStateCollector listener = new SuiteStateCollector();
        listener.onSuiteStarted();
        listener.onTestFound("TestClass", TestId.ROOT, "testOne");
        listener.onTestStarted("TestClass", TestId.ROOT);
        listener.onTestFinished("TestClass", TestId.ROOT);
        listener.onSuiteFinished();

        SuiteResults results = listener.getState();
        assertThat("total", results.getTotalTests(), is(1));
        assertThat("passing", results.getPassingTests(), is(1));
        assertThat("failing", results.getFailingTests(), is(0));
    }

    @Test
    public void suite_with_one_failing_test() {
        SuiteStateCollector listener = new SuiteStateCollector();
        listener.onSuiteStarted();
        listener.onTestFound("TestClass", TestId.ROOT, "testOne");
        listener.onTestStarted("TestClass", TestId.ROOT);
        listener.onFailure("TestClass", TestId.ROOT, new Exception("dummy exception"));
        listener.onTestFinished("TestClass", TestId.ROOT);
        listener.onSuiteFinished();

        SuiteResults results = listener.getState();
        assertThat("total", results.getTotalTests(), is(1));
        assertThat("passing", results.getPassingTests(), is(0));
        assertThat("failing", results.getFailingTests(), is(1));
    }

    // TODO: multiple test classes
}
