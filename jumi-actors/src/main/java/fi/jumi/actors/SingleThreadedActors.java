// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.EventizerProvider;
import fi.jumi.actors.logging.MessageLogger;

import javax.annotation.concurrent.*;
import java.util.List;
import java.util.concurrent.*;

@NotThreadSafe
public class SingleThreadedActors extends Actors {

    private final List<MessageProcessor> actorThreads = new CopyOnWriteArrayList<MessageProcessor>();
    private final MessageLogger logger;

    public SingleThreadedActors(EventizerProvider eventizerProvider, MessageLogger logger) {
        super(eventizerProvider, logger);
        this.logger = logger;
    }

    @Override
    protected void startActorThread(MessageProcessor actorThread) {
        actorThreads.add(actorThread);
    }

    public void processEventsUntilIdle() {
        // TODO: simplify by moving exception handling logic into processNextMessageIfAny()
        boolean idle;
        do {
            idle = true;
            for (MessageProcessor actorThread : actorThreads) {
                try {
                    if (actorThread.processNextMessageIfAny()) {
                        idle = false;
                    }
                } catch (InterruptedException e) {
                    idle = false; // XXX: line not tested
                    Thread.currentThread().interrupt();
                } catch (Throwable t) {
                    idle = false;
                    handleUncaughtException(actorThread, t);
                }

                if (Thread.interrupted()) {
                    actorThreads.remove(actorThread);
                }
            }
        } while (!idle);
    }

    protected void handleUncaughtException(Object source, Throwable uncaughtException) {
        throw new Error("uncaught exception from " + source, uncaughtException);
    }

    public Executor getExecutor() {
        return logger.getLoggedExecutor(new AsynchronousExecutor());
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
