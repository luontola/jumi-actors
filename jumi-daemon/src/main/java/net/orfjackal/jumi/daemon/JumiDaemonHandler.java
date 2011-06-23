// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.daemon;

import net.orfjackal.jumi.core.*;
import net.orfjackal.jumi.core.actors.*;
import net.orfjackal.jumi.core.commands.AddSuiteListener;
import net.orfjackal.jumi.core.events.SuiteEventSender;
import org.jboss.netty.channel.*;

public class JumiDaemonHandler extends SimpleChannelHandler {
    private MessageSender<Event<CommandListener>> toController;

    public JumiDaemonHandler(MessageSender<Event<CommandListener>> toController) {
        this.toController = toController;
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // TODO: notify the controller on disconnect
        SuiteListener listener = new SuiteEventSender(new ChannelMessageSender(e.getChannel()));
        toController.send(new AddSuiteListener(listener));
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        toController.send((Event<CommandListener>) e.getMessage());
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
