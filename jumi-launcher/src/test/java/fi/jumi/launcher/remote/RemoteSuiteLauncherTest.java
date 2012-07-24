// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.*;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.launcher.*;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class RemoteSuiteLauncherTest {

    private final CommandListener daemon = mock(CommandListener.class);
    private final MessageSender<Event<CommandListener>> senderToDaemon = new CommandListenerEventizer().newBackend(daemon);
    private final SpyDaemonSummoner daemonSummoner = new SpyDaemonSummoner();

    private final RemoteSuiteLauncher suiteLauncher =
            new RemoteSuiteLauncher(new FakeActorThread(), ActorRef.<DaemonSummoner>wrap(daemonSummoner));

    private final SuiteOptions suiteOptions = new SuiteOptions();
    private final MessageQueue<Event<SuiteListener>> suiteListener = new MessageQueue<Event<SuiteListener>>();

    @Test
    public void sends_RunTests_command_to_the_daemon_when_it_connects() {
        suiteOptions.classPath.add(new File("dependency.jar"));
        suiteOptions.testsToIncludePattern = "*Test";

        suiteLauncher.runTests(suiteOptions, suiteListener);
        callback().tell().onConnected(senderToDaemon);

        verify(daemon).runTests(suiteOptions.classPath, suiteOptions.testsToIncludePattern);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void forwards_messages_from_daemon_to_the_SuiteListener() {
        Event<SuiteListener> expectedEvent = mock(Event.class);
        suiteLauncher.runTests(suiteOptions, suiteListener);
        callback().tell().onConnected(senderToDaemon);

        callback().tell().onMessage(expectedEvent);

        assertThat(suiteListener.poll(), is(expectedEvent));
    }


    // helpers

    private ActorRef<DaemonListener> callback() {
        return daemonSummoner.lastListener;
    }

    private static class SpyDaemonSummoner implements DaemonSummoner {

        public ActorRef<DaemonListener> lastListener;

        @Override
        public void connectToDaemon(SuiteOptions suiteOptions, ActorRef<DaemonListener> listener) {
            lastListener = listener;
        }
    }
}
