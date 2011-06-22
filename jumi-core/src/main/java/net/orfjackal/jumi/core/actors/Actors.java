// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

public class Actors {
    private final ListenerFactory<?> factory;

    public Actors(ListenerFactory<?> factory) {
        this.factory = factory;
    }

    public <T> T createNewActor(Class<T> type, T target, String name) {
        ListenerFactory<T> factory = getFactoryForType(type);

        MessageQueue<Event<T>> queue = new MessageQueue<Event<T>>();
        MessageSender<Event<T>> receiver = factory.createReceiver(target);
        T handle = factory.createSenderWrapper(queue);

        EventPoller<T> actor = new EventPoller<T>(queue, receiver);
        Thread t = new Thread(actor, name);
        t.start();

        return type.cast(handle);
    }

    @SuppressWarnings({"unchecked"})
    private <T> ListenerFactory<T> getFactoryForType(Class<T> type) {
        // TODO: support for multiple factories
        ListenerFactory<T> factory = (ListenerFactory<T>) this.factory;
        assert factory.getType().equals(type);
        return factory;
    }

    private static class EventPoller<T> implements Runnable {
        private final MessageQueue<Event<T>> queue;
        private final MessageSender<Event<T>> receiver;

        public EventPoller(MessageQueue<Event<T>> queue, MessageSender<Event<T>> receiver) {
            this.queue = queue;
            this.receiver = receiver;
        }

        public void run() {
            try {
                Event<T> message = queue.take();
                receiver.send(message);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

