// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.*;
import fi.jumi.core.*;
import fi.jumi.core.events.suite.SuiteListenerFactory;
import org.jboss.netty.channel.*;

import javax.annotation.concurrent.*;

@ThreadSafe
public class JumiDaemonHandler extends SimpleChannelHandler {
    private final CommandListener coordinator;

    public JumiDaemonHandler(CommandListener coordinator) {
        this.coordinator = coordinator;
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // TODO: notify the coordinator on disconnect
        SuiteListener listener = new SuiteListenerFactory().newFrontend(new ChannelMessageSender(e.getChannel()));
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
