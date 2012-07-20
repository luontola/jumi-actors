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
import fi.jumi.launcher.network.*;
import org.junit.*;

import static org.mockito.Mockito.*;

public class RemoteSuiteLauncherTest {

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new DynamicEventizerProvider(),
            new CrashEarlyFailureHandler(),
            new NullMessageListener()
    );
    private final ActorThread actorThread = actors.startActorThread();

    private final MessageQueue<Event<SuiteListener>> suiteListener = new MessageQueue<Event<SuiteListener>>();
    private final MessagesToDaemon daemon = mock(MessagesToDaemon.class);

    private final FakeDaemonRemote daemonRemote = new FakeDaemonRemote();
    private final ActorRef<DaemonSummoner> daemonRemoteRef = actorThread.bindActor(DaemonSummoner.class, this.daemonRemote);

    private final RemoteSuiteLauncher suiteRemote = new RemoteSuiteLauncher(actorThread, daemonRemoteRef);
    private final ActorRef<SuiteLauncher> suiteRemoteRef = actorThread.bindActor(SuiteLauncher.class, suiteRemote);
    private final SuiteOptions suiteOptions = new SuiteOptions();


    @Test
    public void connects_to_a_daemon_and_sends_RunTests_command_to_the_connected_daemon() {
        suiteRemoteRef.tell().runTests(suiteOptions, suiteListener);

        actors.processEventsUntilIdle();
        verify(daemon).runTests(suiteOptions);
    }

    @Ignore("location of the responsibility unsure")
    @Test
    public void forwards_suite_events_to_listener() {
//        RemoteSuiteLauncher suiteRemote = new RemoteSuiteLauncher(actorThread, ActorRef.wrap(daemonRemote));
//        suiteRemote.runTests(suiteListener);
//        suiteRemote.onDaemonConnected(ActorRef.wrap(daemon));

        // TODO: should we forward the MessageQueue like we do now, or send a mediator to the MessagesToDaemon?
        // MessageReceiver<Event<SuiteListener>>
    }


    private class FakeDaemonRemote implements DaemonSummoner {

        @Override
        public void connectToDaemon(SuiteOptions suiteOptions, ActorRef<MessagesFromDaemon> listener) {
            listener.tell().onDaemonConnected(ActorRef.wrap(daemon));
        }
    }
}
