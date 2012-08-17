// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.network.*;
import fi.jumi.daemon.timeout.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DaemonNetworkEndpoint implements NetworkEndpoint<Event<CommandListener>, Event<SuiteListener>> {

    private final ActorRef<CommandListener> coordinator;
    private final VacancyTimeout connections;

    public DaemonNetworkEndpoint(ActorRef<CommandListener> coordinator, Timeout idleTimeout) {
        this.coordinator = coordinator;
        this.connections = new VacancyTimeout(idleTimeout);
    }

    @Override
    public void onConnected(NetworkConnection connection, MessageSender<Event<SuiteListener>> sender) {
        connections.checkIn();
        // TODO: notify the coordinator on disconnect
        SuiteListener listener = new SuiteListenerEventizer().newFrontend(sender);
        coordinator.tell().addSuiteListener(listener);
    }

    @Override
    public void onMessage(Event<CommandListener> message) {
        message.fireOn(coordinator.tell());
    }

    @Override
    public void onDisconnected() {
        connections.checkOut();
    }
}
