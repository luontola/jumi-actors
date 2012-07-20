// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;

import javax.annotation.concurrent.ThreadSafe;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

@ThreadSafe
public class SocketLauncherConnector {

    public static void connectToLauncher(int launcherPort, final ActorRef<CommandListener> coordinator) {
        ChannelFactory factory = new OioClientSocketChannelFactory(Executors.newCachedThreadPool());
        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        @ThreadSafe
        class MyChannelPipelineFactory implements ChannelPipelineFactory {
            @Override
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.softCachingResolver(Main.class.getClassLoader())),
                        new LoggingHandler(InternalLogLevel.INFO), // TODO: remove this debug code
                        new DaemonChannelHandler(coordinator));
            }
        }
        bootstrap.setPipelineFactory(new MyChannelPipelineFactory());

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        bootstrap.connect(new InetSocketAddress("localhost", launcherPort));
    }

    @ThreadSafe
    private static class DaemonChannelHandler extends SimpleChannelHandler {
        private final ActorRef<CommandListener> coordinator;

        public DaemonChannelHandler(ActorRef<CommandListener> coordinator) {
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
