// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

@ThreadSafe
public class MessageQueue<T> implements MessageSender<T>, MessageReceiver<T> {

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    public void send(T message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public T take() throws InterruptedException {
        return queue.take();
    }

    public T poll() {
        return queue.poll();
    }
}
