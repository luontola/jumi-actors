// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import org.jboss.netty.channel.Channel;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class NettyNetworkConnectionAdapter implements NetworkConnection {

    private final Channel channel;

    public NettyNetworkConnectionAdapter(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void disconnect() {
        channel.disconnect();
    }
}
