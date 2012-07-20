// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiLauncher;
import org.junit.*;

import static fi.jumi.core.util.AsyncAssert.assertEventually;
import static fi.jumi.test.util.ProcessMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class DaemonProcessTest {

    private static final int TIMEOUT = 2000;

    @Rule
    public final AppRunner app = new AppRunner();


    @Ignore("not implemented")
    @Test(timeout = TIMEOUT)
    public void daemon_process_can_be_closed_by_sending_it_a_shutdown_command() throws Exception {
        JumiLauncher launcher = app.getLauncher();
        launcher.start();
        Process process = app.getDaemonProcess();
        assertThat(process, isAlive());

        launcher.shutdown();

        assertEventually(process, isDead(), TIMEOUT / 2);
    }
}
