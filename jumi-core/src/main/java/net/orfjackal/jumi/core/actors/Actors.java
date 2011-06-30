// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import java.util.*;

public class Actors {
    private final ListenerFactory<?>[] factories;
    private final Set<Thread> actorThreads = Collections.synchronizedSet(new HashSet<Thread>());
    private final ThreadLocal<MessageQueue<Event<?>>> queueOfCurrentActor = new ThreadLocal<MessageQueue<Event<?>>>();

    public Actors(ListenerFactory<?>... factories) {
        this.factories = factories;
    }

    public <T> T createNewActor(Class<T> type, T target, String name) {
        ListenerFactory<T> factory = getFactoryForType(type);

        MessageQueue<Event<T>> queue = new MessageQueue<Event<T>>();
        MessageSender<Event<T>> receiver = factory.newBackend(target);
        T handle = factory.newFrontend(queue);

        startActorThread(new EventPoller<T>(queue, receiver), name);
        return type.cast(handle);
    }

    public <T> T bindToCurrentActor(Class<T> type, final T target) {
        ListenerFactory<T> factory = getFactoryForType(type);
        final MessageQueue<Event<?>> queue = getQueueOfCurrentActor();

        T handle = factory.newFrontend(new MessageSender<Event<T>>() {
            public void send(Event<T> message) {
                queue.send(new CustomTargetEvent<T>(message, target));
            }
        });
        return type.cast(handle);
    }

    private MessageQueue<Event<?>> getQueueOfCurrentActor() {
        MessageQueue<Event<?>> queue = queueOfCurrentActor.get();
        if (queue == null) {
            throw new IllegalStateException("queue not set up; maybe we are not inside an actor?");
        }
        return queue;
    }

    private void startActorThread(Runnable actor, String name) {
        Thread t = new Thread(actor, name);
        t.start();
        actorThreads.add(t);
    }

    @SuppressWarnings({"unchecked"})
    private <T> ListenerFactory<T> getFactoryForType(Class<T> type) {
        for (ListenerFactory<?> factory : factories) {
            if (factory.getType().equals(type)) {
                return (ListenerFactory<T>) factory;
            }
        }
        throw new IllegalArgumentException("unsupported listener type: " + type);
    }

    public void shutdown(long timeout) throws InterruptedException {
        for (Thread t : actorThreads) {
            t.interrupt();
        }
        for (Thread t : actorThreads) {
            t.join(timeout);
        }
    }

    private class EventPoller<T> implements Runnable {
        private final MessageQueue<Event<T>> queue;
        private final MessageSender<Event<T>> receiver;

        public EventPoller(MessageQueue<Event<T>> queue, MessageSender<Event<T>> receiver) {
            this.queue = queue;
            this.receiver = receiver;
        }

        @SuppressWarnings({"unchecked"})
        public void run() {
            queueOfCurrentActor.set((MessageQueue) queue);
            try {
                while (!Thread.interrupted()) {
                    Event<T> message = queue.take();
                    receiver.send(message);
                }
            } catch (InterruptedException e) {
                // actor was told to exit
            } finally {
                queueOfCurrentActor.remove();
            }
        }
    }

    private static class CustomTargetEvent<T> implements Event<Object> {
        private final Event<T> message;
        private final T target;

        public CustomTargetEvent(Event<T> message, T target) {
            this.message = message;
            this.target = target;
        }

        public void fireOn(Object ignored) {
            message.fireOn(target);
        }
    }
}
