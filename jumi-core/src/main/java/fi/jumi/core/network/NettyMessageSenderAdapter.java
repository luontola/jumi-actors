// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import fi.jumi.actors.queue.MessageSender;
import org.jboss.netty.channel.Channel;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class NettyMessageSenderAdapter<T> implements MessageSender<T> {

    private final Channel channel;

    public NettyMessageSenderAdapter(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void send(T message) {
        channel.write(message);
    }
}
