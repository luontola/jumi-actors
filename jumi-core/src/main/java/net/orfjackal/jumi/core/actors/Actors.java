// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

public abstract class Actors implements LongLivedActors, OnDemandActors {

    private final ListenerFactory<?>[] factories;
    private final ThreadLocal<MessageQueue<Event<?>>> queueOfCurrentActor = new ThreadLocal<MessageQueue<Event<?>>>();

    public Actors(ListenerFactory<?>... factories) {
        this.factories = factories;
    }

    public <T> T startEventPoller(Class<T> type, T target, String name) {
        checkNotInsideAnActor();
        ListenerFactory<T> factory = getFactoryForType(type);

        MessageQueue<Event<T>> queue = new MessageQueue<Event<T>>();
        MessageSender<Event<T>> receiver = factory.newBackend(target);
        T handle = factory.newFrontend(queue);

        doStartEventPoller(name, queue, receiver);
        return type.cast(handle);
    }

    private <T> void checkNotInsideAnActor() {
        if (queueOfCurrentActor.get() != null) {
            throw new IllegalStateException("already inside an actor");
        }
    }

    protected abstract <T> void doStartEventPoller(String name, MessageQueue<Event<T>> queue, MessageSender<Event<T>> receiver);

    public void startUnattendedWorker(Runnable worker, Runnable onFinished) {
        Runnable onFinishedHandle = bindSecondaryInterface(Runnable.class, onFinished);
        doStartUnattendedWorker(new UnattendedWorker(worker, onFinishedHandle));
    }

    protected abstract void doStartUnattendedWorker(Runnable worker);

    public <T> T bindSecondaryInterface(Class<T> type, final T target) {
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

    protected void initActorContext(MessageQueue<?> queue) {
        queueOfCurrentActor.set((MessageQueue) queue);
    }

    protected void clearActorContext() {
        queueOfCurrentActor.remove();
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


    protected class ActorContext<T> implements Runnable {
        private final MessageQueue<Event<T>> queue;
        private final Runnable actor;

        public ActorContext(MessageQueue<Event<T>> queue, Runnable actor) {
            this.actor = actor;
            this.queue = queue;
        }

        @SuppressWarnings({"unchecked"})
        public void run() {
            initActorContext(queue);
            try {
                actor.run();
            } finally {
                clearActorContext();
            }
        }
    }

    private static class UnattendedWorker implements Runnable {
        private final Runnable worker;
        private final Runnable onFinished;

        public UnattendedWorker(Runnable worker, Runnable onFinished) {
            this.worker = worker;
            this.onFinished = onFinished;
        }

        public void run() {
            try {
                worker.run();
            } finally {
                onFinished.run();
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
            // TODO: double-check that we are on the right thread?
            message.fireOn(target);
        }
    }
}
