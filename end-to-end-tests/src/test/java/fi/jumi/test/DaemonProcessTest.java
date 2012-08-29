// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.core.network.*;
import fi.jumi.launcher.JumiLauncher;
import org.junit.*;

import java.io.IOException;
import java.util.Scanner;

import static fi.jumi.core.util.AsyncAssert.assertEventually;
import static fi.jumi.test.util.ProcessMatchers.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DaemonProcessTest {

    @Rule
    public final AppRunner app = new AppRunner();

    private JumiLauncher launcher;
    private Process daemonProcess;

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void daemon_process_prints_the_program_name_and_version_number_on_startup() throws Exception {
        startDaemonProcess();

        assertThat(firstLine(app.getCurrentDaemonOutput())).matches("Jumi " + BuildTest.VERSION_PATTERN + " starting up");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void daemon_process_can_be_closed_by_sending_it_a_shutdown_command() throws Exception {
        startDaemonProcess();

        launcher.shutdownDaemon();

        assertEventually(daemonProcess, is(dead()), Timeouts.ASSERTION);
        assertThat(app.getFinishedDaemonOutput(), containsString("The system will now exit: ordered to shut down"));
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void daemon_process_will_exit_after_a_timeout_after_all_clients_disconnect() throws Exception {
        launcher = app.getLauncher();
        launcher.setIdleTimeout(0);
        startDaemonProcess();

        launcher.close();

        assertEventually(daemonProcess, is(dead()), Timeouts.ASSERTION);
        assertThat(app.getFinishedDaemonOutput(), containsString("The system will now exit: timed out after everybody disconnected"));
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void daemon_process_will_exit_if_it_cannot_connect_to_the_launcher_on_startup() throws Exception {
        app.setMockNetworkServer(new NonFunctionalNetworkServer());
        launcher = app.getLauncher();
        launcher.setStartupTimeout(0);

        startDaemonProcessAsynchronously();

        assertEventually(daemonProcess, is(dead()), Timeouts.ASSERTION);
        assertThat(app.getFinishedDaemonOutput(), containsString("The system will now exit: timed out before anybody connected"));
    }


    // helpers

    private void startDaemonProcess() throws Exception {
        app.runTests("unimportant");
        initTestHelpers();
    }

    private void startDaemonProcessAsynchronously() throws Exception {
        app.startTests("unimportant");
        initTestHelpers();
    }

    private void initTestHelpers() throws Exception {
        launcher = app.getLauncher();
        daemonProcess = app.getDaemonProcess();
        assertThat(daemonProcess, is(alive()));
    }

    private static String firstLine(String output) {
        return new Scanner(output).nextLine();
    }

    private static class NonFunctionalNetworkServer implements NetworkServer {
        @Override
        public <In, Out> int listenOnAnyPort(NetworkEndpointFactory<In, Out> endpointFactory) {
            return 10; // unassigned port according to http://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
        }

        @Override
        public void close() throws IOException {
        }
    }
}
