// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.*;
import fi.jumi.launcher.network.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class LauncherNetworkEndpoint implements NetworkEndpoint<Event<SuiteListener>, Event<CommandListener>> {

    private final ActorThread currentThread;
    private final ActorRef<MessagesFromDaemon> listener;

    public LauncherNetworkEndpoint(ActorThread currentThread, ActorRef<MessagesFromDaemon> listener) {
        this.currentThread = currentThread;
        this.listener = listener;
    }

    @Override
    public void onConnected(MessageSender<Event<CommandListener>> sender) {
        listener.tell().onDaemonConnected(actor(new SocketMessagesToDaemon(sender)));
    }

    @Override
    public void onMessage(Event<SuiteListener> message) {
        listener.tell().onMessageFromDaemon(message);
    }

    private ActorRef<MessagesToDaemon> actor(SocketMessagesToDaemon rawActor) {
        return currentThread.bindActor(MessagesToDaemon.class, rawActor);
    }
}
