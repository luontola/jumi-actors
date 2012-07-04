// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.EventizerProvider;
import fi.jumi.actors.failures.FailureHandler;
import fi.jumi.actors.logging.MessageListener;

import javax.annotation.concurrent.*;
import java.util.List;
import java.util.concurrent.*;

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
