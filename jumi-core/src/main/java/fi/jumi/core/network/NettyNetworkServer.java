// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;

import javax.annotation.concurrent.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

@Immutable
public class NettyNetworkServer implements NetworkServer {

    @Override
    public <In, Out> int listenOnAnyPort(NetworkEndpoint<In, Out> endpoint) {
        Channel ch = listen(0, endpoint);
        InetSocketAddress addr = (InetSocketAddress) ch.getLocalAddress();
        return addr.getPort();
    }

    private <In, Out> Channel listen(int port, final NetworkEndpoint<In, Out> endpoint) {
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
                        new ObjectDecoder(ClassResolvers.softCachingResolver(getClass().getClassLoader())),
                        new NettyNetworkEndpointAdapter<In, Out>(endpoint));
            }
        }
        bootstrap.setPipelineFactory(new MyChannelPipelineFactory());

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        return bootstrap.bind(new InetSocketAddress(port));
    }
}
