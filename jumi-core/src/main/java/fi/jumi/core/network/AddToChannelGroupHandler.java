// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class AddToChannelGroupHandler extends SimpleChannelUpstreamHandler {

    private final ChannelGroup group;

    public AddToChannelGroupHandler(ChannelGroup group) {
        this.group = group;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        group.add(e.getChannel());
    }
}
