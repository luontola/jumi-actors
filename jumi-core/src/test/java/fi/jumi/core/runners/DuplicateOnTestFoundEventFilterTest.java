// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runs.RunId;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.*;

public class DuplicateOnTestFoundEventFilterTest {

    private final TestClassListener target = mock(TestClassListener.class);
    private final DuplicateOnTestFoundEventFilter filter = new DuplicateOnTestFoundEventFilter(target);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void forwards_unique_onTestFound_events() {
        filter.onTestFound(TestId.ROOT, "root");
        filter.onTestFound(TestId.of(1), "testOne");

        verify(target).onTestFound(TestId.ROOT, "root");
        verify(target).onTestFound(TestId.of(1), "testOne");
        verifyNoMoreInteractions(target);
    }

    @Test
    public void forwards_all_other_events() {
        // TODO: create a generic test which calls all methods except onTestFound
        filter.onPrintedOut(new RunId(8), "stdout");
        filter.onFailure(new RunId(9), TestId.of(1), new Exception("dummy exception"));
        filter.onTestStarted(new RunId(10), TestId.of(2));
        filter.onTestFinished(new RunId(11), TestId.of(3));
        filter.onRunStarted(new RunId(20));
        filter.onRunFinished(new RunId(21));

        verify(target).onPrintedOut(new RunId(8), "stdout");
        verify(target).onFailure(eq(new RunId(9)), eq(TestId.of(1)), notNull(Throwable.class));
        verify(target).onTestStarted(new RunId(10), TestId.of(2));
        verify(target).onTestFinished(new RunId(11), TestId.of(3));
        verify(target).onRunStarted(new RunId(20));
        verify(target).onRunFinished(new RunId(21));
        verifyNoMoreInteractions(target);
    }

    @Test
    public void removes_duplicate_onTestFound_events() {
        filter.onTestFound(TestId.ROOT, "root");
        filter.onTestFound(TestId.ROOT, "root");

        verify(target, times(1)).onTestFound(TestId.ROOT, "root");
        verifyNoMoreInteractions(target);
    }

    @Test
    public void tests_must_be_found_always_with_the_same_name() {
        filter.onTestFound(TestId.ROOT, "first name");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("test TestId() was already found with another name: first name");
        filter.onTestFound(TestId.ROOT, "second name");
    }

    @Test
    public void parents_must_be_found_before_their_children() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("parent of TestId(0) must be found first");
        filter.onTestFound(TestId.of(0), "child");
    }
}
