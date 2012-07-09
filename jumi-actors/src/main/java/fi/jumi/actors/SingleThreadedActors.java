// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.EventizerProvider;
import fi.jumi.actors.listeners.*;

import javax.annotation.concurrent.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Single-threaded actors container for testing. The {@link ActorThread}s are <em>not</em> backed by real threads -
 * instead they will process messages when the {@link #processEventsUntilIdle()} method is called.
 */
@NotThreadSafe
public class SingleThreadedActors extends Actors {

    private final List<MessageProcessor> actorThreads = new CopyOnWriteArrayList<MessageProcessor>();
    private final MessageListener messageListener;

    public SingleThreadedActors(EventizerProvider eventizerProvider, FailureHandler failureHandler, MessageListener messageListener) {
        super(eventizerProvider, failureHandler, messageListener);
        this.messageListener = messageListener;
    }

    @Override
    protected void startActorThread(MessageProcessor actorThread) {
        actorThreads.add(actorThread);
    }

    /**
     * Processes in the current thread all messages which were sent to actors. The order of processing messages is
     * deterministic. Will block until all messages have been processed and nobody is sending more messages.
     * <p/>
     * When using {@link CrashEarlyFailureHandler}, will rethrow uncaught exceptions from actors to the caller of this
     * method.
     */
    public void processEventsUntilIdle() {
        boolean idle;
        do {
            idle = true;
            for (MessageProcessor actorThread : actorThreads) {
                if (actorThread.processNextMessageIfAny()) {
                    idle = false;
                }
                if (Thread.interrupted()) {
                    actorThreads.remove(actorThread);
                }
            }
        } while (!idle);
    }

    /**
     * Returns an asynchronous {@link Executor} which works the same way as all the actors in this container. Useful in
     * tests to have asynchrony without the non-determinism of real threads.
     *
     * @see #processEventsUntilIdle()
     */
    public Executor getExecutor() {
        return messageListener.getListenedExecutor(new AsynchronousExecutor());
    }


    @ThreadSafe
    private class AsynchronousExecutor implements Executor {
        @Override
        public void execute(final Runnable command) {
            // To unify the concepts of an executor and actors,
            // we implement the executor as one-time actor threads.
            ActorThread actorThread = startActorThread();
            ActorRef<Runnable> actor = actorThread.bindActor(Runnable.class, command);
            actor.tell().run();
            actorThread.stop();
        }
    }
}
