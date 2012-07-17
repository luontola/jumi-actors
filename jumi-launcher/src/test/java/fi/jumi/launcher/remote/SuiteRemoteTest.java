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

public class SuiteRemoteTest {

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new DynamicEventizerProvider(),
            new CrashEarlyFailureHandler(),
            new NullMessageListener()
    );
    private final ActorThread actorThread = actors.startActorThread();

    private final MessageQueue<Event<SuiteListener>> suiteListener = new MessageQueue<Event<SuiteListener>>();
    private final DaemonConnection daemonConnection = mock(DaemonConnection.class);

    private final FakeDaemonRemote daemonRemote = new FakeDaemonRemote();
    private final ActorRef<DaemonRemote> daemonRemoteRef = actorThread.bindActor(DaemonRemote.class, this.daemonRemote);

    private final SuiteRemoteImpl suiteRemote = new SuiteRemoteImpl(actorThread, daemonRemoteRef);
    private final ActorRef<SuiteRemote> suiteRemoteRef = actorThread.bindActor(SuiteRemote.class, suiteRemote);
    private final SuiteOptions suiteOptions = new SuiteOptions();


    @Test
    public void connects_to_a_daemon_and_sends_RunTests_command_to_the_connected_daemon() {
        suiteRemoteRef.tell().runTests(suiteOptions, suiteListener);

        actors.processEventsUntilIdle();
        verify(daemonConnection).runTests(suiteOptions, suiteListener);
    }

    @Ignore("location of the responsibility unsure")
    @Test
    public void forwards_suite_events_to_listener() {
//        SuiteRemoteImpl suiteRemote = new SuiteRemoteImpl(actorThread, ActorRef.wrap(daemonRemote));
//        suiteRemote.runTests(suiteListener);
//        suiteRemote.onDaemonConnected(ActorRef.wrap(daemonConnection));

        // TODO: should we forward the MessageQueue like we do now, or send a mediator to the DaemonConnection?
        // MessageReceiver<Event<SuiteListener>>
    }


    private class FakeDaemonRemote implements DaemonRemote {

        @Override
        public void connectToDaemon(SuiteOptions suiteOptions, ActorRef<DaemonConnectionListener> response) {
            response.tell().onDaemonConnected(ActorRef.wrap(daemonConnection));
        }
    }
}
