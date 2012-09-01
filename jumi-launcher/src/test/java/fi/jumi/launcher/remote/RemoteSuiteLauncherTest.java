// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.*;
import fi.jumi.core.config.*;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.launcher.FakeActorThread;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class RemoteSuiteLauncherTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final CommandListener daemon = mock(CommandListener.class);
    private final MessageSender<Event<CommandListener>> senderToDaemon = new CommandListenerEventizer().newBackend(daemon);
    private final SpyDaemonSummoner daemonSummoner = new SpyDaemonSummoner();
    private final DaemonConfiguration dummyDaemonConfiguration = new DaemonConfigurationBuilder().freeze();

    private final RemoteSuiteLauncher suiteLauncher =
            new RemoteSuiteLauncher(new FakeActorThread(), ActorRef.<DaemonSummoner>wrap(daemonSummoner));

    private final MessageQueue<Event<SuiteListener>> suiteListener = new MessageQueue<Event<SuiteListener>>();

    @Test
    public void sends_RunTests_command_to_the_daemon_when_it_connects() {
        SuiteConfiguration config = new SuiteConfigurationBuilder()
                .addToClassPath(new File("dependency.jar"))
                .includedTestsPattern("*Test")
                .build();

        suiteLauncher.runTests(config, dummyDaemonConfiguration, suiteListener);
        callback().tell().onConnected(null, senderToDaemon);

        verify(daemon).runTests(config.classPath(), config.includedTestsPattern());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void forwards_messages_from_daemon_to_the_SuiteListener() {
        Event<SuiteListener> expectedEvent = mock(Event.class);
        suiteLauncher.runTests(dummyConfig(), dummyDaemonConfiguration, suiteListener);
        callback().tell().onConnected(null, senderToDaemon);

        callback().tell().onMessage(expectedEvent);

        assertThat(suiteListener.poll(), is(expectedEvent));
    }

    @Test
    public void can_send_shutdown_command_to_the_daemon() {
        suiteLauncher.runTests(dummyConfig(), dummyDaemonConfiguration, suiteListener);
        callback().tell().onConnected(null, senderToDaemon);

        suiteLauncher.shutdownDaemon();

        verify(daemon).shutdown();
    }

    @Test
    public void shutdown_command_fails_if_the_daemon_is_not_connected() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("daemon not connected");

        suiteLauncher.shutdownDaemon();
    }


    // helpers

    private static SuiteConfiguration dummyConfig() {
        return new SuiteConfigurationBuilder().build();
    }

    private ActorRef<DaemonListener> callback() {
        return daemonSummoner.lastListener;
    }

    private static class SpyDaemonSummoner implements DaemonSummoner {

        public ActorRef<DaemonListener> lastListener;

        @Override
        public void connectToDaemon(SuiteConfiguration suiteConfiguration,
                                    DaemonConfiguration daemonConfiguration,
                                    ActorRef<DaemonListener> listener) {
            lastListener = listener;
        }
    }
}
