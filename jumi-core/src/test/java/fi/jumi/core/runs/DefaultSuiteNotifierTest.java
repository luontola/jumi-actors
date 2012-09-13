// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;


import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.*;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.runners.TestClassListener;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.PrintStream;
import java.nio.charset.Charset;

import static org.mockito.Mockito.*;

public class DefaultSuiteNotifierTest {

    private static final RunId FIRST_RUN_ID = new RunId(RunId.FIRST_ID);

    private final TestClassListener listener = mock(TestClassListener.class);
    private final OutputCapturer outputCapturer = new OutputCapturer(new PrintStream(new NullOutputStream()), new PrintStream(new NullOutputStream()), Charset.defaultCharset());
    private final PrintStream stdout = outputCapturer.out();

    private final SuiteNotifier notifier = new DefaultSuiteNotifier(ActorRef.wrap(listener), new RunIdSequence(), outputCapturer);

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
        inOrder.verify(listener).onTestFinished(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onTestFinished(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onRunFinished(FIRST_RUN_ID);
    }

    @Test
    public void captures_what_is_printed_during_a_run() {
        TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        stdout.print("1");
        TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));
        stdout.print("2");
        tn2.fireTestFinished();
        stdout.print("3");
        tn1.fireTestFinished();

        verify(listener).onPrintedOut(FIRST_RUN_ID, "1");
        verify(listener).onPrintedOut(FIRST_RUN_ID, "2");
        verify(listener).onPrintedOut(FIRST_RUN_ID, "3");
    }

    @Test
    public void does_not_capture_what_is_printed_outside_a_run() {
        stdout.print("before");
        TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        tn1.fireTestFinished();
        stdout.print("after");

        verify(listener, never()).onPrintedOut(any(RunId.class), anyString());
    }
}
