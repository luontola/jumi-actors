// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.network;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.Event;
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

    private final ActorThread currentThread;

    public SocketDaemonConnector(ActorThread currentThread) {
        this.currentThread = currentThread;
    }

    @Override
    public int listenForDaemonConnection(ActorRef<DaemonConnectionListener> listener) {
        final DaemonConnectorHandler handler = new DaemonConnectorHandler(listener);

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

    private ActorRef<DaemonConnection> actor(NettyDaemonConnection rawActor) {
        return currentThread.bindActor(DaemonConnection.class, rawActor);
    }


    @ThreadSafe
    public class DaemonConnectorHandler extends SimpleChannelHandler {

        private final ActorRef<DaemonConnectionListener> listener;

        public DaemonConnectorHandler(ActorRef<DaemonConnectionListener> listener) {
            this.listener = listener;
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            listener.tell().onDaemonConnected(actor(new NettyDaemonConnection(e.getChannel())));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            listener.tell().onMessageFromDaemon((Event<SuiteListener>) e.getMessage());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            // TODO: better error handling
            e.getCause().printStackTrace();
            e.getChannel().close();
        }
    }
}
