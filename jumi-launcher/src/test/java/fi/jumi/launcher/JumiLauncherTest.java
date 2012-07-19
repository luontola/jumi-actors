// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.daemon.HomeManager;
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
        ActorThread actorThread = actors.startActorThread();

        ActorRef<DaemonRemote> daemonRemote = actorThread.bindActor(DaemonRemote.class, new DaemonRemoteImpl(
                new DummyHomeManager(),
                processStarter,
                daemonConnector,
                new NullWriter()
        ));
        ActorRef<SuiteRemote> suiteRemote = actorThread.bindActor(SuiteRemote.class, new SuiteRemoteImpl(actorThread, daemonRemote));

        launcher = new JumiLauncher(suiteRemote);
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
        public int listenForDaemonConnection(ActorRef<DaemonConnectionListener> listener) {
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
