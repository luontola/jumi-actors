// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OutputCapturerInstallerTest {

    private final OutputCapturer capturer = new OutputCapturer(new NullOutputStream(), new NullOutputStream(), Charset.defaultCharset());
    private final FakeOutErr outErr = new FakeOutErr();
    private final OutputCapturerInstaller installer = new OutputCapturerInstaller(outErr);

    @Test
    public void replaces_stdout_with_the_captured_stream() {
        installer.install(capturer);

        assertThat(outErr.out(), is(capturer.out()));
    }

    @Test
    public void replaces_stderr_with_the_captured_stream() {
        installer.install(capturer);

        assertThat(outErr.err(), is(capturer.err()));
    }
}
