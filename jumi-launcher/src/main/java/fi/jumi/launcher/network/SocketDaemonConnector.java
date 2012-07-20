// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.network;

import fi.jumi.actors.queue.MessageSender;
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
    public <In, Out> int listenForDaemonConnection(final NetworkEndpoint<In, Out> endpoint) {
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
                        new NetworkEndpointAdapter<In, Out>(endpoint));
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
    private static class NetworkEndpointAdapter<In, Out> extends SimpleChannelHandler {

        private final NetworkEndpoint<In, Out> endpoint;

        public NetworkEndpointAdapter(NetworkEndpoint<In, Out> endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            endpoint.onConnected(new ChannelMessageSender<Out>(e.getChannel()));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            endpoint.onMessage((In) e.getMessage());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            // TODO: better error handling
            e.getCause().printStackTrace();
            e.getChannel().close();
        }
    }

    @ThreadSafe
    private static class ChannelMessageSender<T> implements MessageSender<T> {

        private final Channel channel;

        public ChannelMessageSender(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void send(T message) {
            channel.write(message);
        }
    }
}
