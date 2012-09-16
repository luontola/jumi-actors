// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PlainTextPrinterTest {

    private final StringBuilder output = new StringBuilder();
    private final PlainTextPrinter printer = new PlainTextPrinter(output);

    @Test
    public void prints_stdout() {
        printer.printOut("foo");

        assertThat(output.toString(), is("foo"));
    }

    @Test
    public void prints_stderr() {
        printer.printErr("foo");

        assertThat(output.toString(), is("foo"));
    }

    @Test
    public void prints_meta_lines() {
        printer.printlnMeta("foo");

        assertThat(output.toString(), is("foo\n"));
    }

    @Test
    public void if_previous_stdout_did_not_end_with_newline_then_meta_goes_on_a_new_line() {
        printer.printOut("out");
        printer.printlnMeta("meta");

        assertThat(output.toString(), is("out\nmeta\n"));
    }

    @Test
    public void if_previous_stdout_ended_with_Unix_newline_then_meta_is_printed_right_after_it() {
        printer.printOut("out\n");
        printer.printlnMeta("meta");

        assertThat(output.toString(), is("out\nmeta\n"));
    }

    @Test
    public void if_previous_stdout_ended_with_DOS_newline_then_meta_is_printed_right_after_it() {
        printer.printOut("out\r\n");
        printer.printlnMeta("meta");

        assertThat(output.toString(), is("out\r\nmeta\n"));
    }
}
