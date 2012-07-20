// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.network.NetworkEndpoint;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DaemonNetworkEndpoint implements NetworkEndpoint<Event<CommandListener>, Event<SuiteListener>> {

    private final ActorRef<CommandListener> coordinator;

    public DaemonNetworkEndpoint(ActorRef<CommandListener> coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public void onConnected(MessageSender<Event<SuiteListener>> sender) {
        // TODO: notify the coordinator on disconnect
        SuiteListener listener = new SuiteListenerEventizer().newFrontend((MessageSender<Event<SuiteListener>>) sender);
        coordinator.tell().addSuiteListener(listener);
    }

    @Override
    public void onMessage(Event<CommandListener> message) {
        message.fireOn(coordinator.tell());
    }
}
