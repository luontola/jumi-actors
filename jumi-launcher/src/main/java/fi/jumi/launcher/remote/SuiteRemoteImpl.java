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
public class SuiteRemoteImpl implements DaemonConnectionListener, SuiteRemote {

    private final ActorThread currentThread;
    private final ActorRef<DaemonRemote> daemonRemote;

    private SuiteOptions suiteOptions;
    private MessageSender<Event<SuiteListener>> suiteListener;

    public SuiteRemoteImpl(ActorThread currentThread, ActorRef<DaemonRemote> daemonRemote) {
        this.currentThread = currentThread;
        this.daemonRemote = daemonRemote;
    }

    @Override
    public void runTests(SuiteOptions suiteOptions, MessageSender<Event<SuiteListener>> suiteListener) {
        this.suiteOptions = suiteOptions;
        this.suiteListener = suiteListener;
        daemonRemote.tell().connectToDaemon(suiteOptions, self());
    }

    @Override
    public void onDaemonConnected(ActorRef<DaemonConnection> daemon) {
        assert suiteOptions != null; // TODO: remove me
        assert suiteListener != null; // TODO: remove me
        daemon.tell().runTests(suiteOptions, suiteListener);
    }


    // actor helpers

    private ActorRef<DaemonConnectionListener> self() {
        return currentThread.bindActor(DaemonConnectionListener.class, this);
    }
}
