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
import java.util.concurrent.Executors;

@ThreadSafe
public class NettyNetworkClient implements NetworkClient {

    private final InternalLogLevel logLevel;
    private final ChannelGroup allChannels = new DefaultChannelGroup();

    public NettyNetworkClient() {
        this(false);
    }

    public NettyNetworkClient(boolean logging) {
        this.logLevel = logging ? InternalLogLevel.INFO : InternalLogLevel.DEBUG;
    }

    @Override
    public <In, Out> void connect(String hostname, int port, final NetworkEndpoint<In, Out> endpoint) {
        ChannelFactory factory = new OioClientSocketChannelFactory(Executors.newCachedThreadPool());
        ClientBootstrap bootstrap = new ClientBootstrap(factory);

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

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        bootstrap.connect(new InetSocketAddress(hostname, port));
    }

    @Override
    public void close() {
        // TODO: call releaseExternalResources
        allChannels.close().awaitUninterruptibly();
    }
}
