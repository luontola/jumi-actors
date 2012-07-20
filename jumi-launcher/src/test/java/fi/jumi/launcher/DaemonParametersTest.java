// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.daemon.Steward;
import fi.jumi.launcher.network.*;
import fi.jumi.launcher.process.ProcessStarter;
import fi.jumi.launcher.remote.*;
import org.apache.commons.io.output.NullWriter;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class DaemonParametersTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private final StubDaemonConnector daemonConnector = new StubDaemonConnector();
    private final SpySuiteLauncher suiteRemote = new SpySuiteLauncher();

    private final JumiLauncher launcher = new JumiLauncher(ActorRef.<SuiteLauncher>wrap(suiteRemote));

    @Test
    public void tells_daemon_process_the_launcher_port_number() throws Exception {
        daemonConnector.port = 123;

        launcher.start();

        assertThat(daemonConfig().launcherPort, is(123));
    }

    @Test
    public void can_enable_message_logging() throws Exception {
        launcher.start();

        assertThat(daemonConfig().logActorMessages, is(false));

        launcher.enableMessageLogging();
        launcher.start();

        assertThat(daemonConfig().logActorMessages, is(true));
    }


    // helpers

    private Configuration daemonConfig() {
        SpyProcessStarter processStarter = new SpyProcessStarter();

        ProcessStartingDaemonSummoner daemonRemote = new ProcessStartingDaemonSummoner(
                mock(Steward.class),
                processStarter,
                daemonConnector,
                new NullWriter()
        );
        daemonRemote.connectToDaemon(suiteRemote.suiteOptions, null);

        return Configuration.parse(processStarter.lastArgs, processStarter.lastSystemProperties);
    }

    private static class SpyProcessStarter implements ProcessStarter {

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
        public int listenForDaemonConnection(ActorRef<MessagesFromDaemon> listener) {
            return port;
        }
    }

    private static class SpySuiteLauncher implements SuiteLauncher {
        public SuiteOptions suiteOptions;
        public MessageSender<Event<SuiteListener>> suiteListener;

        @Override
        public void runTests(SuiteOptions suiteOptions, MessageSender<Event<SuiteListener>> suiteListener) {
            this.suiteOptions = suiteOptions;
            this.suiteListener = suiteListener;
        }
    }
}
