// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import org.jboss.netty.channel.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class NettyNetworkEndpointAdapter<In, Out> extends SimpleChannelHandler {

    private final NetworkEndpoint<In, Out> endpoint;

    public NettyNetworkEndpointAdapter(NetworkEndpoint<In, Out> endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Channel channel = e.getChannel();
        endpoint.onConnected(new NettyNetworkConnectionAdapter(channel), new NettyMessageSenderAdapter<Out>(channel));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        endpoint.onMessage((In) e.getMessage());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        endpoint.onDisconnected();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
