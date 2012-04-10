// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;


import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

public class SuiteNotifierTest {

    private static final RunId FIRST_RUN_ID = new RunId(RunId.FIRST_ID);

    private final TestClassListener listener = mock(TestClassListener.class);
    private final SuiteNotifier notifier = new DefaultSuiteNotifier(listener, new RunIdSequence());

    @Test
    public void notifies_about_the_beginning_and_end_of_a_run() {
        TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));
        tn2.fireTestFinished();
        tn1.fireTestFinished();

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onRunStarted(FIRST_RUN_ID);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onTestFinished(TestId.of(0));
        inOrder.verify(listener).onTestFinished(TestId.ROOT);
        inOrder.verify(listener).onRunFinished(FIRST_RUN_ID);
    }
}
