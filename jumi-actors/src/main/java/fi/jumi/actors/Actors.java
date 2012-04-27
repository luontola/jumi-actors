// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.mq.*;

import javax.annotation.concurrent.*;
import java.util.concurrent.Executor;

@ThreadSafe
public abstract class Actors {

    private final Eventizer<?>[] factories;
    private final ThreadLocal<ActorThread> currentActorThread = new ThreadLocal<ActorThread>();

    public Actors(Eventizer<?>... factories) {
        this.factories = factories;
    }

    public ActorThread startActorThread(String name) {
        checkNotInsideAnActor();
        ActorThreadImpl actorThread = new ActorThreadImpl();
        startActorThread(name, actorThread);
        return actorThread;
    }

    private void checkNotInsideAnActor() {
        if (currentActorThread.get() != null) {
            throw new IllegalStateException("already inside an actor");
        }
    }

    protected abstract void startActorThread(String name, MessageProcessor actorThread);

    public void startUnattendedWorker(Runnable worker, Runnable onFinished) {
        ActorThread actorThread = getCurrentActorThread();
        ActorRef<Runnable> onFinishedHandle = actorThread.bindActor(Runnable.class, onFinished);
        doStartUnattendedWorker(new UnattendedWorker(worker, onFinishedHandle));
    }

    protected abstract void doStartUnattendedWorker(Runnable worker);

    public ActorThread getCurrentActorThread() {
        ActorThread actorThread = currentActorThread.get();
        if (actorThread == null) {
            throw new IllegalStateException("We are not inside an actor");
        }
        return actorThread;
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


    @ThreadSafe
    private class ActorThreadImpl implements ActorThread, Executor, MessageProcessor {

        private final MessageQueue<Runnable> taskQueue = new MessageQueue<Runnable>();

        @Override
        public <T> ActorRef<T> bindActor(Class<T> type, T rawActor) {
            Eventizer<T> factory = getFactoryForType(type);
            T proxy = factory.newFrontend(new MessageToActorSender<T>(this, rawActor));
            return ActorRef.wrap(type.cast(proxy));
        }

        @Override
        public void execute(Runnable task) {
            taskQueue.send(task);
        }

        @Override
        public void processNextMessage() throws InterruptedException {
            Runnable task = taskQueue.take();
            process(task);
        }

        @Override
        public boolean processNextMessageIfAny() {
            Runnable task = taskQueue.poll();
            if (task == null) {
                return false;
            }
            process(task);
            return true;
        }

        private void process(Runnable task) {
            currentActorThread.set(this);
            try {
                task.run();
            } finally {
                currentActorThread.remove();
            }
        }
    }

    @NotThreadSafe
    private static class UnattendedWorker implements Runnable { // TODO: decouple workers from actors
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
    private static class MessageToActorSender<T> implements MessageSender<Event<T>> {
        private final Executor actorThread;
        private final T rawActor;

        public MessageToActorSender(Executor actorThread, T rawActor) {
            this.actorThread = actorThread;
            this.rawActor = rawActor;
        }

        @Override
        public void send(final Event<T> message) {
            actorThread.execute(new MessageToActor<T>(rawActor, message));
        }
    }

    @NotThreadSafe
    private static class MessageToActor<T> implements Runnable {
        private T rawActor;
        private final Event<T> message;

        public MessageToActor(T rawActor, Event<T> message) {
            this.rawActor = rawActor;
            this.message = message;
        }

        @Override
        public void run() {
            message.fireOn(rawActor);
        }

        @Override
        public String toString() {
            // TODO: write a test
            return "MessageToActor(" + rawActor.getClass().getName() + ", " + message + ")";
        }
    }
}
