// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.EventizerProvider;
import fi.jumi.actors.listeners.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Executor;

@ThreadSafe
public class MultiThreadedActors extends Actors {

    private final Executor executor;

    public MultiThreadedActors(Executor executor, EventizerProvider eventizerProvider, FailureHandler failureHandler, MessageListener messageListener) {
        super(eventizerProvider, failureHandler, messageListener);
        this.executor = executor;
    }

    @Override
    protected void startActorThread(MessageProcessor actorThread) {
        executor.execute(new BlockingActorProcessor(actorThread));
    }


    @ThreadSafe
    private static class BlockingActorProcessor implements Runnable {
        private final MessageProcessor actorThread;

        public BlockingActorProcessor(MessageProcessor actorThread) {
            this.actorThread = actorThread;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    actorThread.processNextMessage();
                }
            } catch (InterruptedException e) {
                // actor was told to exit
            }
        }
    }
}
