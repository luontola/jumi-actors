// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.daemon;

import net.orfjackal.jumi.core.SuiteListener;
import net.orfjackal.jumi.core.actors.MessageSender;
import net.orfjackal.jumi.core.commands.*;
import net.orfjackal.jumi.core.events.*;
import org.jboss.netty.channel.*;

public class JumiDaemonHandler extends SimpleChannelHandler {
    private MessageSender<Command> toController;

    public JumiDaemonHandler(MessageSender<Command> toController) {
        this.toController = toController;
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // TODO: notify the controller on disconnect
        SuiteListener listener = new SuiteEventSender(new ChannelMessageSender(e.getChannel()));
        toController.send(new AddSuiteListener(listener));
    }

    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        toController.send((Command) e.getMessage());
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    private static class ChannelMessageSender implements MessageSender<SuiteEvent> {
        private final Channel channel;

        public ChannelMessageSender(Channel channel) {
            this.channel = channel;
        }

        public void send(SuiteEvent message) {
            channel.write(message);
        }
    }
}
