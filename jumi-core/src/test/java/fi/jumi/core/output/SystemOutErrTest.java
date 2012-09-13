// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.*;

import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SystemOutErrTest {

    private final SystemOutErr systemOutErr = new SystemOutErr();

    private final PrintStream originalStdout = System.out;
    private final PrintStream originalStderr = System.err;

    @After
    public void restoreStdoutStderr() {
        System.setOut(originalStdout);
        System.setErr(originalStderr);
    }

    @Test
    public void gets_the_real_stdout() {
        assertThat(systemOutErr.out(), is(System.out));
    }

    @Test
    public void gets_the_real_stderr() {
        assertThat(systemOutErr.err(), is(System.err));
    }

    @Test
    public void sets_the_real_stdout() {
        PrintStream newStream = new PrintStream(new NullOutputStream());

        systemOutErr.setOut(newStream);

        assertThat(System.out, is(newStream));
    }

    @Test
    public void sets_the_real_stderr() {
        PrintStream newStream = new PrintStream(new NullOutputStream());

        systemOutErr.setErr(newStream);

        assertThat(System.err, is(newStream));
    }
}
