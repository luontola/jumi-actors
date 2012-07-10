// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.network.DaemonConnector;
import fi.jumi.launcher.process.ProcessLauncher;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JumiLauncherTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void tells_daemon_process_the_launcher_port_number() throws IOException {
        int expectedPort = 123;
        SpyProcessLauncher processLauncher = new SpyProcessLauncher();
        JumiLauncher launcher = new JumiLauncher(new StubDaemonConnector(expectedPort), processLauncher, tempDir.newFolder());

        launcher.start();

        Configuration config = Configuration.parse(processLauncher.lastArgs);
        assertThat(config.launcherPort, is(expectedPort));
    }


    private static class SpyProcessLauncher implements ProcessLauncher {

        public String[] lastArgs;

        @Override
        public Process startJavaProcess(File workingDir, List<String> jvmOptions, File executableJar, String... args) throws IOException {
            lastArgs = args;
            return new FakeProcess();
        }
    }

    private static class StubDaemonConnector implements DaemonConnector {

        private final int port;

        public StubDaemonConnector(int port) {
            this.port = port;
        }

        @Override
        public int listenForDaemonConnection(MessageSender<Event<SuiteListener>> eventTarget, List<File> classPath, String testsToIncludePattern) {
            return port;
        }
    }
}
