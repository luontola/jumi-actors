// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.daemon.HomeManager;
import fi.jumi.launcher.network.DaemonConnector;
import fi.jumi.launcher.process.ProcessLauncher;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JumiLauncherTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private final SpyProcessLauncher processLauncher = new SpyProcessLauncher();
    private final StubDaemonConnector daemonConnector = new StubDaemonConnector();
    private JumiLauncher launcher;

    @Before
    public void setup() throws IOException {
        launcher = new JumiLauncher(new DummyHomeManager(), daemonConnector, processLauncher);
    }

    @Test
    public void tells_daemon_process_the_launcher_port_number() throws IOException {
        daemonConnector.port = 123;

        launcher.start();

        assertThat(daemonConfig().launcherPort, is(123));
    }

    @Test
    public void can_enable_message_logging() throws IOException {
        launcher.start();

        assertThat(daemonConfig().logActorMessages, is(false));

        launcher.enableMessageLogging();
        launcher.start();

        assertThat(daemonConfig().logActorMessages, is(true));
    }


    // helpers

    private Configuration daemonConfig() {
        return Configuration.parse(processLauncher.lastArgs, processLauncher.lastSystemProperties);
    }

    private static class SpyProcessLauncher implements ProcessLauncher {

        public String[] lastArgs;
        public Properties lastSystemProperties = new Properties();

        @Override
        public Process startJavaProcess(File executableJar, File workingDir, List<String> jvmOptions, Properties systemProperties, String... args) throws IOException {
            lastArgs = args;
            lastSystemProperties = systemProperties;
            return new FakeProcess();
        }
    }

    private static class StubDaemonConnector implements DaemonConnector {

        public int port = 42;

        @Override
        public int listenForDaemonConnection(MessageSender<Event<SuiteListener>> eventTarget, List<File> classPath, String testsToIncludePattern) {
            return port;
        }
    }

    private static class DummyHomeManager implements HomeManager {

        @Override
        public File getSettingsDir() {
            return new File("dummy-settings-dir");
        }

        @Override
        public File getDaemonJar() {
            return new File("dummy-daemon.jar");
        }
    }
}
