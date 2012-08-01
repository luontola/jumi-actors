// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;

import javax.annotation.concurrent.*;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

@Immutable
public class NettyNetworkServer implements NetworkServer {

    private final DefaultChannelGroup allChannels = new DefaultChannelGroup();

    private final InternalLogLevel logLevel;
    private final ChannelFactory channelFactory;

    public NettyNetworkServer() {
        this(false);
    }

    public NettyNetworkServer(boolean logging) {
        this(logging, Executors.newCachedThreadPool());
    }

    public NettyNetworkServer(boolean logging, ExecutorService workerExecutor) {
        this.logLevel = logging ? InternalLogLevel.INFO : InternalLogLevel.DEBUG;
        this.channelFactory = new OioServerSocketChannelFactory(workerExecutor, workerExecutor);
    }

    @Override
    public <In, Out> int listenOnAnyPort(NetworkEndpoint<In, Out> endpoint) {
        Channel ch = listen(0, endpoint);
        InetSocketAddress addr = (InetSocketAddress) ch.getLocalAddress();
        return addr.getPort();
    }

    private <In, Out> Channel listen(int port, final NetworkEndpoint<In, Out> endpoint) {
        ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

        @ThreadSafe
        class MyChannelPipelineFactory implements ChannelPipelineFactory {
            @Override
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.softCachingResolver(getClass().getClassLoader())),
                        new LoggingHandler(logLevel),
                        new NettyNetworkEndpointAdapter<In, Out>(endpoint),
                        new AddToChannelGroupHandler(allChannels));
            }
        }
        bootstrap.setPipelineFactory(new MyChannelPipelineFactory());

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        Channel channel = bootstrap.bind(new InetSocketAddress(port));
        allChannels.add(channel);
        return channel;
    }

    @Override
    public void close() {
        allChannels.close().awaitUninterruptibly();
        channelFactory.releaseExternalResources();
    }
}
