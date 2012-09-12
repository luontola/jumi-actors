// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class OutputCapturerTest {

    private final StringWriter printedToOut = new StringWriter();
    private final PrintStream realOut = new PrintStream(new WriterOutputStream(printedToOut));

    private final OutputCapturer capturer = new OutputCapturer(realOut);

    // TODO: the same tests for stderr

    @Test
    public void passes_through_stdout_to_the_real_stdout() {
        capturer.out().print("foo");
        capturer.out().write('.');

        assertThat(printedToOut.toString(), is("foo."));
    }

    @Test
    public void captures_standard_output() {
        OutputListener listener = mock(OutputListener.class);

        capturer.captureTo(listener);
        capturer.out().print("foo");
        capturer.out().write('.');

        verify(listener).out("foo");
        verify(listener).out(".");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void after_starting_a_new_capture_all_new_events_to_to_the_new_output_listener() {
        OutputListener listener1 = mock(OutputListener.class, "listener1");
        OutputListener listener2 = mock(OutputListener.class, "listener2");

        capturer.captureTo(listener1);
        capturer.captureTo(listener2);
        capturer.out().print("foo");

        verifyZeroInteractions(listener1);
        verify(listener2).out("foo");
    }

    @Test
    public void starting_a_new_capture_does_not_require_installing_a_new_PrintStream_to_SystemOut() {
        OutputListener listener = mock(OutputListener.class);

        PrintStream out = capturer.out();
        capturer.captureTo(listener);
        out.print("foo");

        verify(listener).out("foo");
    }
}
