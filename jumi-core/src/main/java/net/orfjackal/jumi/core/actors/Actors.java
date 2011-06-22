// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import java.util.concurrent.*;

public class Actors {
    private final ListenerFactory<?> factory;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Actors(ListenerFactory<?> factory) {
        this.factory = factory;
    }

    public <T> T createNewActor(Class<T> type, T target, String name) {
        ListenerFactory<T> factory = getFactoryForType(type);

        MessageQueue<Event<T>> queue = new MessageQueue<Event<T>>();
        MessageSender<Event<T>> receiver = factory.createReceiver(target);
        T handle = factory.createSenderWrapper(queue);

        executor.execute(new EventPoller<T>(queue, receiver, name));

        return type.cast(handle);
    }

    @SuppressWarnings({"unchecked"})
    private <T> ListenerFactory<T> getFactoryForType(Class<T> type) {
        ListenerFactory<T> factory = (ListenerFactory<T>) this.factory;
        assert factory.getType().equals(type);
        return factory;
    }

    public void shutdown(long timeout, TimeUnit unit) throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(timeout, unit);
    }

    private static class EventPoller<T> implements Runnable {
        private final MessageQueue<Event<T>> queue;
        private final MessageSender<Event<T>> receiver;
        private final String name;

        public EventPoller(MessageQueue<Event<T>> queue, MessageSender<Event<T>> receiver, String name) {
            this.queue = queue;
            this.receiver = receiver;
            this.name = name;
        }

        public void run() {
            Thread.currentThread().setName(name);
            try {
                while (true) {
                    Event<T> message = queue.take();
                    receiver.send(message);
                }
            } catch (InterruptedException e) {
                System.out.println("Shutting down actor " + name);
            }
        }
    }
}

