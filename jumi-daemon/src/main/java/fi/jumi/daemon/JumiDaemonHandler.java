// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.*;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import org.jboss.netty.channel.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class JumiDaemonHandler extends SimpleChannelHandler {
    private final ActorRef<CommandListener> coordinator;

    public JumiDaemonHandler(ActorRef<CommandListener> coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // TODO: notify the coordinator on disconnect
        SuiteListener listener = new SuiteListenerEventizer().newFrontend(new ChannelMessageSender(e.getChannel()));
        coordinator.tell().addSuiteListener(listener);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Event<CommandListener> event = (Event<CommandListener>) e.getMessage();
        event.fireOn(coordinator.tell());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    @ThreadSafe
    private static class ChannelMessageSender implements MessageSender<Event<SuiteListener>> {
        private final Channel channel;

        public ChannelMessageSender(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void send(Event<SuiteListener> message) {
            channel.write(message);
        }
    }
}
