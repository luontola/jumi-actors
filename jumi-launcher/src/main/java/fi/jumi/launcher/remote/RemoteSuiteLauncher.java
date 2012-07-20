// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.SuiteListener;
import fi.jumi.launcher.SuiteOptions;
import fi.jumi.launcher.network.*;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class RemoteSuiteLauncher implements MessagesFromDaemon, SuiteLauncher {

    private final ActorThread currentThread;
    private final ActorRef<DaemonSummoner> daemonSummoner;

    private SuiteOptions suiteOptions;
    private MessageSender<Event<SuiteListener>> suiteListener;

    public RemoteSuiteLauncher(ActorThread currentThread, ActorRef<DaemonSummoner> daemonSummoner) {
        this.currentThread = currentThread;
        this.daemonSummoner = daemonSummoner;
    }

    @Override
    public void runTests(SuiteOptions suiteOptions, MessageSender<Event<SuiteListener>> suiteListener) {
        this.suiteOptions = suiteOptions;
        this.suiteListener = suiteListener;
        daemonSummoner.tell().connectToDaemon(suiteOptions, self());
    }

    @Override
    public void onDaemonConnected(ActorRef<MessagesToDaemon> daemon) {
        assert suiteOptions != null; // TODO: remove me
        daemon.tell().runTests(suiteOptions);
    }

    @Override
    public void onMessageFromDaemon(Event<SuiteListener> message) {
        suiteListener.send(message);
    }


    // actor helpers

    private ActorRef<MessagesFromDaemon> self() {
        return currentThread.bindActor(MessagesFromDaemon.class, this);
    }
}
