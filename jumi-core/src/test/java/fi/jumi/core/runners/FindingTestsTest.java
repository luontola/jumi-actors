// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class FindingTestsTest {

    private final Class<DummyTest> testClass = DummyTest.class;

    private TestClassRunnerListener listener = mock(TestClassRunnerListener.class);
    private TestClassRunner runner = new TestClassRunner(testClass, null, listener, null, null);
    private SuiteNotifier notifier = new DefaultSuiteNotifier(runner);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void finding_tests_multiple_times_is_idempotent() {
        notifier.fireTestFound(TestId.ROOT, "root");
        notifier.fireTestFound(TestId.ROOT, "root");

        assertThat(runner.getTestNames().size(), is(1));
    }

    @Test
    public void tests_must_be_found_always_with_the_same_name() {
        notifier.fireTestFound(TestId.ROOT, "name 1");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("test TestId() was already found with another name: name 1");
        notifier.fireTestFound(TestId.ROOT, "name 2");
    }

    @Test
    public void parents_must_be_found_before_children() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("parent of TestId(0) must be found first");
        notifier.fireTestFound(TestId.of(0), "child");
    }

    // TODO: should younger siblings be found first?

    @Test
    public void notifies_the_listener_about_found_tests() {
        notifier.fireTestFound(TestId.ROOT, "root");

        verify(listener).onTestFound(TestId.ROOT, "root");
    }

    @Test
    public void notifies_the_listener_about_found_tests_only_once() {
        notifier.fireTestFound(TestId.ROOT, "root");
        notifier.fireTestFound(TestId.ROOT, "root");

        verify(listener, atMost(1)).onTestFound(TestId.ROOT, "root");
    }


    private class DummyTest {
    }
}
