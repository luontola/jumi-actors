// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;


import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AssigningRunIdsTest {

    private static final RunId RUN_ID_1 = new RunId(RunId.FIRST_ID);
    private static final RunId RUN_ID_2 = new RunId(RunId.FIRST_ID + 1);

    private final TestClassListener listener = mock(TestClassListener.class);
    private final RunIdSequence runIdSequence = new RunIdSequence();
    private final SuiteNotifier notifier = new DefaultSuiteNotifier(listener, runIdSequence);

    @Test
    public void RunId_is_assigned_when_a_test_is_started() {
        notifier.fireTestStarted(TestId.ROOT);

        verify(listener).onTestStarted(RUN_ID_1, TestId.ROOT);
    }

    @Test
    public void nested_tests_get_the_same_RunId() {
        notifier.fireTestStarted(TestId.ROOT);

        notifier.fireTestStarted(TestId.of(0));

        verify(listener).onTestStarted(RUN_ID_1, TestId.of(0));
    }

    @Test
    public void siblings_of_nested_tests_get_the_same_RunId() {
        notifier.fireTestStarted(TestId.ROOT);
        notifier.fireTestStarted(TestId.of(0))
                .fireTestFinished();

        notifier.fireTestStarted(TestId.of(1));

        verify(listener).onTestStarted(RUN_ID_1, TestId.of(1));
    }

    @Test
    public void nested_tests_in_child_threads_get_the_same_RunId() {
        // TODO
    }

    @Test
    public void new_runs_get_a_different_RunId() {
        notifier.fireTestStarted(TestId.ROOT)
                .fireTestFinished();

        notifier.fireTestStarted(TestId.of(0));

        verify(listener).onTestStarted(RUN_ID_2, TestId.of(0));
    }

    @Test
    public void concurrent_runs_in_other_threads_get_a_different_RunId() {
        // TODO
    }
}
