// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.mq;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;
import java.util.logging.*;

@ThreadSafe
public class MessageQueue<T> implements MessageSender<T>, MessageReceiver<T> {

    private static final Logger logger = Logger.getLogger(MessageQueue.class.getName());

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    @Override
    public void send(T message) {
        logger.log(Level.FINE, "SEND {0}", message);
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T take() throws InterruptedException {
        T message = queue.take();
        logger.log(Level.FINE, "TAKE {0}", message);
        return message;
    }

    @Override
    public T poll() {
        T message = queue.poll();
        logger.log(Level.FINE, "POLL {0}", message);
        return message;
    }
}
