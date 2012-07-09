// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.*;
import fi.jumi.actors.listeners.*;
import fi.jumi.actors.queue.*;

import javax.annotation.concurrent.*;

/**
 * Entry point to this actors library. The actors container which coordinates communication between individual actors.
 * Use one of the implementations of this class.
 *
 * @see MultiThreadedActors
 * @see SingleThreadedActors
 */
@ThreadSafe
public abstract class Actors {

    private final EventizerProvider eventizerProvider;
    private final FailureHandler failureHandler;
    private final MessageListener messageListener;

    public Actors(EventizerProvider eventizerProvider, FailureHandler failureHandler, MessageListener messageListener) {
        this.eventizerProvider = eventizerProvider;
        this.failureHandler = failureHandler;
        this.messageListener = messageListener;
    }

    /**
     * Creates a new {@link ActorThread} for running actors.
     */
    public ActorThread startActorThread() {
        ActorThreadImpl actorThread = new ActorThreadImpl();
        startActorThread(actorThread);
        return actorThread;
    }

    protected abstract void startActorThread(MessageProcessor actorThread);


    @ThreadSafe
    private class ActorThreadImpl implements ActorThread, MessageProcessor {

        private final MessageQueue<Runnable> taskQueue = new MessageQueue<Runnable>();

        @Override
        public <T> ActorRef<T> bindActor(Class<T> type, T rawActor) {
            Eventizer<T> eventizer = eventizerProvider.getEventizerForType(type);
            T proxy = eventizer.newFrontend(new MessageToActorSender<T>(this, rawActor));
            return ActorRef.wrap(type.cast(proxy));
        }

        @Override
        public void stop() {
            taskQueue.send(new PoisonPill());
        }

        public void send(MessageToActor<?> task) {
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
            // MessageToActor should already take care of handling uncaught exceptions,
            // so we don't need to do it here.
            task.run();
        }
    }

    @ThreadSafe
    private class MessageToActorSender<T> implements MessageSender<Event<T>> {
        private final ActorThreadImpl actorThread;
        private final T rawActor;

        public MessageToActorSender(ActorThreadImpl actorThread, T rawActor) {
            this.actorThread = actorThread;
            this.rawActor = rawActor;
        }

        @Override
        public void send(final Event<T> message) {
            messageListener.onMessageSent(message);
            actorThread.send(new MessageToActor<T>(rawActor, message));
        }
    }

    @NotThreadSafe
    private class MessageToActor<T> implements Runnable {
        private final T rawActor;
        private final Event<T> message;

        public MessageToActor(T rawActor, Event<T> message) {
            this.rawActor = rawActor;
            this.message = message;
        }

        @Override
        public void run() {
            messageListener.onProcessingStarted(rawActor, message);
            try {
                message.fireOn(rawActor);
            } catch (Throwable t) {
                failureHandler.uncaughtException(rawActor, message, t);
            } finally {
                messageListener.onProcessingFinished();
            }
        }
    }

    @Immutable
    private static class PoisonPill implements Runnable {

        @Override
        public void run() {
            Thread.currentThread().interrupt();
        }
    }
}
