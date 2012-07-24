// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;

import static fi.jumi.core.util.AsyncAssert.assertEventually;
import static fi.jumi.test.util.ProcessMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DaemonProcessTest {

    private static final int TIMEOUT = 2000;

    @Rule
    public final AppRunner app = new AppRunner();


    @Test(timeout = TIMEOUT)
    public void daemon_process_can_be_closed_by_sending_it_a_shutdown_command() throws Exception {
        app.runTests("unimportant");
        Process process = app.getDaemonProcess();
        assertThat(process, is(alive()));

        app.getLauncher().shutdownDaemon();

        assertEventually(process, is(dead()), TIMEOUT / 2);
        // TODO: assert on the message that the daemon prints on shutdown?
    }
}
