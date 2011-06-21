// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class TestClassRunnerTest {

    private SuiteListener listener = mock(SuiteListener.class);
    private TestClassRunner runner = new TestClassRunner(listener);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void remembers_found_tests() {
        SuiteNotifier notifier = runner.getSuiteNotifier();
        notifier.fireTestFound(TestId.ROOT, "root");
        notifier.fireTestFound(TestId.of(0), "child 0");
        notifier.fireTestFound(TestId.of(1), "child 1");

        assertThat(runner.getTestNames(), containsInAnyOrder("root", "child 0", "child 1"));
    }

    @Test
    public void finding_tests_multiple_times_is_idempotent() {
        SuiteNotifier notifier = runner.getSuiteNotifier();
        notifier.fireTestFound(TestId.ROOT, "root");
        notifier.fireTestFound(TestId.ROOT, "root");

        assertThat(runner.getTestNames().size(), is(1));
    }

    @Test
    public void tests_must_be_found_always_with_the_same_name() {
        SuiteNotifier notifier = runner.getSuiteNotifier();
        notifier.fireTestFound(TestId.ROOT, "name 1");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("test TestId() was already found with another name: name 1");
        notifier.fireTestFound(TestId.ROOT, "name 2");
    }

    @Test
    public void parents_must_be_found_before_children() {
        SuiteNotifier notifier = runner.getSuiteNotifier();

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("parent of TestId(0) must be found first");
        notifier.fireTestFound(TestId.of(0), "child");
    }

    // TODO: should younger siblings be found first?

    @Test
    public void notifies_the_listener_about_found_tests() {
        SuiteNotifier notifier = runner.getSuiteNotifier();

        notifier.fireTestFound(TestId.ROOT, "root");

        verify(listener).onTestFound(TestId.ROOT, "root");
    }

    @Test
    public void notifies_the_listener_about_found_tests_only_once() {
        SuiteNotifier notifier = runner.getSuiteNotifier();

        notifier.fireTestFound(TestId.ROOT, "root");
        notifier.fireTestFound(TestId.ROOT, "root");

        verify(listener, atMost(1)).onTestFound(TestId.ROOT, "root");
    }
}
