// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.network;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.core.*;
import fi.jumi.core.network.*;
import fi.jumi.daemon.DaemonNetworkEndpoint;
import fi.jumi.launcher.SuiteOptions;
import fi.jumi.launcher.remote.*;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SocketDaemonConnectorTest {

    private static final long TIMEOUT = 1000;

    private final SpyMessagesFromDaemon launcherSide = new SpyMessagesFromDaemon();
    private final SpyCommandListener daemonSide = new SpyCommandListener();

    // TODO: disconnect at the end of a test

    @Test(timeout = TIMEOUT)
    public void launcher_can_send_messages_to_daemon() throws Exception {
        connectDaemonToLauncher();

        launcherSide.toDaemon.get().tell().runTests(new SuiteOptions());

        assertThat(daemonSide.eventsReceived.take(), is("runTests"));
    }

    @Test(timeout = TIMEOUT)
    public void daemon_can_send_messages_to_launcher() throws Exception {
        connectDaemonToLauncher();

        daemonSide.toLauncher.get().onSuiteStarted();

        assertThat(launcherSide.eventsReceived.take(), is("OnSuiteStartedEvent"));
    }


    private void connectDaemonToLauncher() {
        NettyNetworkServer launcher = new NettyNetworkServer();
        LauncherNetworkEndpoint endpoint = new LauncherNetworkEndpoint(new FakeActorThread(), ActorRef.<MessagesFromDaemon>wrap(launcherSide));
        int port = launcher.listenOnAnyPort(endpoint);
        new NettyNetworkClient().connect("127.0.0.1", port, new DaemonNetworkEndpoint(ActorRef.<CommandListener>wrap(daemonSide)));
    }

    private static class SpyMessagesFromDaemon implements MessagesFromDaemon {
        public final FutureValue<ActorRef<MessagesToDaemon>> toDaemon = new FutureValue<ActorRef<MessagesToDaemon>>();
        public final BlockingQueue<String> eventsReceived = new LinkedBlockingQueue<String>();

        @Override
        public void onDaemonConnected(ActorRef<MessagesToDaemon> daemon) {
            toDaemon.set(daemon);
        }

        @Override
        public void onMessageFromDaemon(Event<SuiteListener> message) {
            eventsReceived.add(message.getClass().getSimpleName());
        }
    }

    private static class SpyCommandListener implements CommandListener {
        public final FutureValue<SuiteListener> toLauncher = new FutureValue<SuiteListener>();
        public final BlockingQueue<String> eventsReceived = new LinkedBlockingQueue<String>();

        @Override
        public void addSuiteListener(SuiteListener listener) {
            toLauncher.set(listener);
        }

        @Override
        public void runTests(List<File> classPath, String testsToIncludePattern) {
            eventsReceived.add("runTests");
        }
    }
}
