// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.daemon;

import net.orfjackal.jumi.core.SuiteListener;
import net.orfjackal.jumi.core.actors.MessageSender;
import net.orfjackal.jumi.core.commands.RunTestsCommand;
import net.orfjackal.jumi.core.events.*;
import org.jboss.netty.channel.*;

public class JumiDaemonHandler extends SimpleChannelHandler {

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object command = e.getMessage();
        if (command instanceof RunTestsCommand) {
            // TODO: move all of this logic outside the network layer
            SuiteListener listener = new SuiteEventSender(new ChannelMessageSender(e.getChannel()));
            listener.onSuiteStarted();
            listener.onSuiteFinished();
        }
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
