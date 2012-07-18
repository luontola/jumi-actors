// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.network;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.SuiteListener;
import fi.jumi.launcher.JumiLauncher;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;

import javax.annotation.concurrent.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

@Immutable
public class SocketDaemonConnector implements DaemonConnector {

    @Override
    public int listenForDaemonConnection(MessageSender<Event<SuiteListener>> eventTarget,
                                         FutureValue<Channel> daemonConnection) {
        final DaemonConnectorHandler handler = new DaemonConnectorHandler(eventTarget, daemonConnection);

        ChannelFactory factory =
                new OioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        @ThreadSafe
        class MyChannelPipelineFactory implements ChannelPipelineFactory {
            @Override
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.softCachingResolver(JumiLauncher.class.getClassLoader())),
                        handler);
            }
        }
        bootstrap.setPipelineFactory(new MyChannelPipelineFactory());

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        Channel ch = bootstrap.bind(new InetSocketAddress(0));
        InetSocketAddress addr = (InetSocketAddress) ch.getLocalAddress();
        return addr.getPort();
    }


    @ThreadSafe
    public static class DaemonConnectorHandler extends SimpleChannelHandler {

        private final MessageSender<Event<SuiteListener>> target;
        private final FutureValue<Channel> daemonConnection;

        public DaemonConnectorHandler(MessageSender<Event<SuiteListener>> target,
                                      FutureValue<Channel> daemonConnection) {
            this.target = target;
            this.daemonConnection = daemonConnection;
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            this.daemonConnection.set(e.getChannel());
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            // TODO: don't call MessageSender, call DaemonConnectionListener#onMessageFromDaemon
            target.send((Event<SuiteListener>) e.getMessage());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            // TODO: better error handling
            e.getCause().printStackTrace();
            e.getChannel().close();
        }
    }
}
