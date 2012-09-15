// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.config.*;
import fi.jumi.core.network.*;
import fi.jumi.launcher.FakeProcess;
import fi.jumi.launcher.daemon.Steward;
import fi.jumi.launcher.process.*;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;

import java.io.*;

import static fi.jumi.core.util.AsyncAssert.assertEventually;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class ProcessStartingDaemonSummonerTest {

    private static final int TIMEOUT = 1000;

    private final Steward steward = mock(Steward.class);
    private final SpyProcessStarter processStarter = new SpyProcessStarter();
    private final SpyNetworkServer daemonConnector = new SpyNetworkServer();
    private final StringWriter outputListener = new StringWriter();

    private final ProcessStartingDaemonSummoner daemonSummoner = new ProcessStartingDaemonSummoner(
            steward,
            processStarter,
            daemonConnector,
            new WriterOutputStream(outputListener)
    );

    private final SuiteConfiguration dummySuiteConfig = new SuiteConfiguration();
    private final DaemonConfiguration dummyDaemonConfig = new DaemonConfiguration();
    private final ActorRef<DaemonListener> dummyListener = ActorRef.wrap(null);

    @Test
    public void tells_to_daemon_the_socket_to_contact() {
        daemonConnector.portToReturn = 123;

        daemonSummoner.connectToDaemon(dummySuiteConfig, dummyDaemonConfig, dummyListener);

        DaemonConfiguration daemonConfig = parseDaemonArguments(processStarter.lastArgs);
        assertThat(daemonConfig.launcherPort(), is(123));
    }

    private static DaemonConfiguration parseDaemonArguments(String[] args) {
        return new DaemonConfigurationBuilder()
                .parseProgramArgs(args)
                .freeze();
    }

    @Test
    public void tells_to_output_listener_what_the_daemon_prints() {
        processStarter.processToReturn.inputStream = new ByteArrayInputStream("hello".getBytes());

        daemonSummoner.connectToDaemon(dummySuiteConfig, dummyDaemonConfig, dummyListener);

        assertEventually(outputListener, hasToString("hello"), TIMEOUT);
    }


    private static class SpyProcessStarter implements ProcessStarter {

        public String[] lastArgs;
        public FakeProcess processToReturn = new FakeProcess();

        @Override
        public Process startJavaProcess(JvmArgs jvmArgs) throws IOException {
            this.lastArgs = jvmArgs.programArgs.toArray(new String[0]);
            return processToReturn;
        }
    }

    private static class SpyNetworkServer implements NetworkServer {

        public int portToReturn = 1;

        @Override
        public <In, Out> int listenOnAnyPort(NetworkEndpointFactory<In, Out> endpointFactory) {
            return portToReturn;
        }

        @Override
        public void close() throws IOException {
        }
    }
}
