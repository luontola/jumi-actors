// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.actors.queue.MessageQueue;
import fi.jumi.core.SuiteListener;
import fi.jumi.launcher.SuiteOptions;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class RemoteSuiteLauncherTest {

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new DynamicEventizerProvider(),
            new CrashEarlyFailureHandler(),
            new NullMessageListener()
    );
    private final ActorThread actorThread = actors.startActorThread();

    private final ActorRef<DaemonSummoner> daemonSummoner =
            actorThread.bindActor(DaemonSummoner.class, new FakeDaemonRemote());
    private final ActorRef<SuiteLauncher> suiteLauncher =
            actorThread.bindActor(SuiteLauncher.class, new RemoteSuiteLauncher(actorThread, daemonSummoner));

    private final SuiteOptions suiteOptions = new SuiteOptions();
    private final MessageQueue<Event<SuiteListener>> suiteListener = new MessageQueue<Event<SuiteListener>>();
    private final MessagesToDaemon messagesToDaemon = mock(MessagesToDaemon.class);
    public ActorRef<MessagesFromDaemon> messagesFromDaemon;

    @Test
    public void sends_RunTests_command_to_the_daemon_when_it_connects() {
        suiteLauncher.tell().runTests(suiteOptions, suiteListener);

        actors.processEventsUntilIdle();
        verify(messagesToDaemon).runTests(suiteOptions);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void forwards_messages_from_daemon_to_the_SuiteListener() {
        suiteLauncher.tell().runTests(suiteOptions, suiteListener);
        actors.processEventsUntilIdle();
        Event<SuiteListener> expectedEvent = mock(Event.class);

        messagesFromDaemon.tell().onMessageFromDaemon(expectedEvent);
        actors.processEventsUntilIdle();

        assertThat(suiteListener.poll(), is(expectedEvent));
    }


    private class FakeDaemonRemote implements DaemonSummoner {

        @Override
        public void connectToDaemon(SuiteOptions suiteOptions, ActorRef<MessagesFromDaemon> listener) {
            messagesFromDaemon = listener;
            listener.tell().onDaemonConnected(ActorRef.wrap(messagesToDaemon));
        }
    }
}
