// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.daemon;

import net.orfjackal.jumi.core.*;
import net.orfjackal.jumi.core.actors.*;
import net.orfjackal.jumi.core.events.SuiteListenerToSuiteEvent;
import org.jboss.netty.channel.*;

public class JumiDaemonHandler extends SimpleChannelHandler {
    private final CommandListener coordinator;

    public JumiDaemonHandler(CommandListener coordinator) {
        this.coordinator = coordinator;
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // TODO: notify the coordinator on disconnect
        SuiteListener listener = new SuiteListenerToSuiteEvent(new ChannelMessageSender(e.getChannel()));
        coordinator.addSuiteListener(listener);
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Event<CommandListener> event = (Event<CommandListener>) e.getMessage();
        event.fireOn(coordinator);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    private static class ChannelMessageSender implements MessageSender<Event<SuiteListener>> {
        private final Channel channel;

        public ChannelMessageSender(Channel channel) {
            this.channel = channel;
        }

        public void send(Event<SuiteListener> message) {
            channel.write(message);
        }
    }
}
