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
        super.channelConnected(ctx, e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        endpoint.onMessage((In) e.getMessage());
        super.messageReceived(ctx, e);
    }

    // XXX: Sometimes the disconnecting is a downstream event and sometimes an upstream event,
    // so we must override both channelDisconnected and disconnectRequested to avoid missing the event.

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        endpoint.onDisconnected();
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void disconnectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        endpoint.onDisconnected();
        super.disconnectRequested(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        System.err.println(getClass().getName() + ": " + e);
        e.getCause().printStackTrace();
        e.getChannel().close();
        super.exceptionCaught(ctx, e);
    }
}
