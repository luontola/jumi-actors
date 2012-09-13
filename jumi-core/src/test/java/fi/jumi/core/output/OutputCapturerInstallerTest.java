// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.PrintStream;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OutputCapturerInstallerTest {

    @Test
    public void replaces_stdout_with_the_captured_stream() {
        OutputCapturer capturer = new OutputCapturer(new PrintStream(new NullOutputStream()), new PrintStream(new NullOutputStream()), Charset.defaultCharset());
        FakeOutErr outErr = new FakeOutErr();
        OutputCapturerInstaller installer = new OutputCapturerInstaller(outErr);

        installer.install(capturer);

        assertThat(outErr.out(), is(capturer.out()));
    }
}
