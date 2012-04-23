// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import javax.annotation.concurrent.*;

@ThreadSafe
public abstract class Actors implements LongLivedActors, OnDemandActors {

    private final Eventizer<?>[] factories;
    private final ThreadLocal<Actor<?>> currentActor = new ThreadLocal<Actor<?>>();

    public Actors(Eventizer<?>... factories) {
        this.factories = factories;
    }

    @Override
    public <T> ActorRef<T> createPrimaryActor(Class<T> type, T target, String name) {
        checkNotInsideAnActor();
        Eventizer<T> factory = getFactoryForType(type);

        Actor<T> actor = new Actor<T>(factory, target);

        startEventPoller(name, actor);
        return ActorRef.wrap(actor.frontend);
    }

    private <T> void checkNotInsideAnActor() {
        if (currentActor.get() != null) {
            throw new IllegalStateException("already inside an actor");
        }
    }

    protected abstract <T> void startEventPoller(String name, Actor<T> actor);

    @Override
    public void startUnattendedWorker(Runnable worker, Runnable onFinished) {
        ActorRef<Runnable> onFinishedHandle = createSecondaryActor(Runnable.class, onFinished);
        doStartUnattendedWorker(new UnattendedWorker(worker, onFinishedHandle));
    }

    protected abstract void doStartUnattendedWorker(Runnable worker);

    @Override
    public <T> ActorRef<T> createSecondaryActor(Class<T> type, T target) {
        Eventizer<T> factory = getFactoryForType(type);
        MessageQueue<Event<?>> queue = (MessageQueue) getCurrentActor().queue;

        T handle = factory.newFrontend(new DelegateToCustomTarget<T>(queue, target));
        return ActorRef.wrap(type.cast(handle));
    }

    private Actor<?> getCurrentActor() {
        Actor<?> actor = currentActor.get();
        if (actor == null) {
            throw new IllegalStateException("We are not inside an actor");
        }
        return actor;
    }

    @SuppressWarnings({"unchecked"})
    private <T> Eventizer<T> getFactoryForType(Class<T> type) {
        for (Eventizer<?> factory : factories) {
            if (factory.getType().equals(type)) {
                return (Eventizer<T>) factory;
            }
        }
        throw new IllegalArgumentException("unsupported listener type: " + type);
    }


    @NotThreadSafe
    protected class Actor<T> {
        private final MessageQueue<Event<T>> queue = new MessageQueue<Event<T>>();
        private final MessageSender<Event<T>> backend;
        public final T frontend;

        public Actor(Eventizer<T> factory, T rawActor) {
            this.backend = factory.newBackend(rawActor);
            this.frontend = factory.newFrontend(queue);
        }

        public void processNextMessage() throws InterruptedException {
            Event<T> message = queue.take();
            process(message);
        }

        public boolean processNextMessageIfAny() {
            Event<T> message = queue.poll();
            if (message == null) {
                return false;
            }
            process(message);
            return true;
        }

        private void process(Event<T> event) {
            currentActor.set(this);
            try {
                backend.send(event);
            } finally {
                currentActor.remove();
            }
        }
    }

    @NotThreadSafe
    private static class UnattendedWorker implements Runnable {
        private final Runnable worker;
        private final ActorRef<Runnable> onFinished;

        public UnattendedWorker(Runnable worker, ActorRef<Runnable> onFinished) {
            this.worker = worker;
            this.onFinished = onFinished;
        }

        @Override
        public void run() {
            try {
                worker.run();
            } finally {
                onFinished.tell().run();
            }
        }
    }

    @ThreadSafe
    private static class DelegateToCustomTarget<T> implements MessageSender<Event<T>> {
        private final MessageQueue<Event<?>> queue;
        private final T target;

        public DelegateToCustomTarget(MessageQueue<Event<?>> queue, T target) {
            this.queue = queue;
            this.target = target;
        }

        @Override
        public void send(Event<T> message) {
            queue.send(new CustomTargetEvent<T>(message, target));
        }
    }

    @ThreadSafe
    private static class CustomTargetEvent<T> implements Event<Object> {
        private final Event<T> message;
        private final T target;

        public CustomTargetEvent(Event<T> message, T target) {
            this.message = message;
            this.target = target;
        }

        @Override
        public void fireOn(Object ignored) {
            // TODO: double-check that we are on the right thread?
            message.fireOn(target);
        }

        @Override
        public String toString() {
            return "CustomTargetEvent(" + target + ", " + message + ")";
        }
    }
}
