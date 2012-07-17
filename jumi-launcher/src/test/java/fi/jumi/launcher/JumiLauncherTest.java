// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.daemon.HomeManager;
import fi.jumi.launcher.network.*;
import fi.jumi.launcher.process.ProcessStarter;
import org.jboss.netty.channel.Channel;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class JumiLauncherTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new DynamicEventizerProvider(),
            new CrashEarlyFailureHandler(),
            new NullMessageListener()
    );
    private final SpyProcessStarter processStarter = new SpyProcessStarter();
    private final StubDaemonConnector daemonConnector = new StubDaemonConnector();
    private JumiLauncher launcher;

    @Before
    public void setup() throws IOException {
        launcher = new JumiLauncher(actors, new DummyHomeManager(), daemonConnector, processStarter);
    }

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
        actors.processEventsUntilIdle();
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
        public int listenForDaemonConnection(MessageSender<Event<SuiteListener>> eventTarget,
                                             FutureValue<Channel> daemonConnection) {
            // XXX: improve the design to make these stubs simpler
            daemonConnection.set(mock(Channel.class));
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
