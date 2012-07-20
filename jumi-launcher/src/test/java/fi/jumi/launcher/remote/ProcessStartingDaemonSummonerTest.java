// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.*;
import fi.jumi.core.network.*;
import fi.jumi.launcher.*;
import fi.jumi.launcher.daemon.Steward;
import fi.jumi.launcher.network.FakeActorThread;
import fi.jumi.launcher.process.ProcessStarter;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.util.*;

import static fi.jumi.core.util.AsyncAssert.assertEventually;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class ProcessStartingDaemonSummonerTest {

    private static final int TIMEOUT = 1000;

    private final ActorThread actorThread = new FakeActorThread();
    private final Steward steward = mock(Steward.class);
    private final SpyProcessStarter processStarter = new SpyProcessStarter();
    private final NetworkServer daemonConnector = mock(NetworkServer.class);
    private final StringWriter outputListener = new StringWriter();

    private final ProcessStartingDaemonSummoner daemonSummoner = new ProcessStartingDaemonSummoner(
            actorThread,
            steward,
            processStarter,
            daemonConnector,
            outputListener
    );

    private final ActorRef<MessagesFromDaemon> daemonListener = ActorRef.wrap(mock(MessagesFromDaemon.class));
    private final SuiteOptions suiteOptions = new SuiteOptions();

    @Test
    public void tells_to_daemon_the_socket_to_contact() {
        stub(daemonConnector.listenOnAnyPort(Mockito.<NetworkEndpoint<?, ?>>any())).toReturn(123);

        daemonSummoner.connectToDaemon(suiteOptions, daemonListener);

        assertThat(processStarter.args, is(hasItemInArray("123")));
    }

    @Test
    public void tells_to_output_listener_what_the_daemon_prints() {
        processStarter.process.inputStream = new ByteArrayInputStream("hello".getBytes());

        daemonSummoner.connectToDaemon(suiteOptions, daemonListener);

        assertEventually(outputListener, hasToString("hello"), TIMEOUT);
    }


    private static class SpyProcessStarter implements ProcessStarter {

        public String[] args;
        public FakeProcess process = new FakeProcess();

        @Override
        public Process startJavaProcess(File executableJar, File workingDir, List<String> jvmOptions, Properties systemProperties, String... args) throws IOException {
            this.args = args;
            return process;
        }
    }
}
