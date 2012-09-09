// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;

import javax.annotation.concurrent.ThreadSafe;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

@ThreadSafe
public class NettyNetworkClient implements NetworkClient {

    private final ChannelGroup allChannels = new DefaultChannelGroup();

    private final InternalLogLevel logLevel;
    private final ChannelFactory channelFactory;

    public NettyNetworkClient() {
        this(false);
    }

    public NettyNetworkClient(boolean logging) {
        this(logging, Executors.newCachedThreadPool());
    }

    public NettyNetworkClient(boolean logging, ExecutorService workerExecutor) {
        this.logLevel = logging ? InternalLogLevel.INFO : InternalLogLevel.DEBUG;
        this.channelFactory = new OioClientSocketChannelFactory(workerExecutor);
    }

    @Override
    public <In, Out> void connect(String hostname, int port, final NetworkEndpoint<In, Out> endpoint) {
        ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);

        @ThreadSafe
        class MyChannelPipelineFactory implements ChannelPipelineFactory {
            @Override
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.softCachingResolver(getClass().getClassLoader())),
                        new LoggingHandler(NettyNetworkClient.class, logLevel),
                        new NettyNetworkEndpointAdapter<>(endpoint),
                        new AddToChannelGroupHandler(allChannels));
            }
        }
        bootstrap.setPipelineFactory(new MyChannelPipelineFactory());

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        bootstrap.connect(new InetSocketAddress(hostname, port));
    }

    @Override
    public void close() {
        allChannels.close().awaitUninterruptibly();
        channelFactory.releaseExternalResources();
    }
}
