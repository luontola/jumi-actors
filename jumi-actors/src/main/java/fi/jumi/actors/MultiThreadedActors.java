// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.EventizerLocator;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

@ThreadSafe
public class MultiThreadedActors extends Actors {

    private final Set<Thread> actorThreads = Collections.synchronizedSet(new HashSet<Thread>());

    public MultiThreadedActors(EventizerLocator eventizerLocator) {
        super(eventizerLocator);
    }

    @Override
    protected void startActorThread(String name, MessageProcessor actorThread) {
        Thread t = new Thread(new BlockingActorProcessor(actorThread), name);
        t.start();
        actorThreads.add(t);
    }

    public void shutdown(long timeout) throws InterruptedException {
        for (Thread t : actorThreads) {
            t.interrupt();
        }
        for (Thread t : actorThreads) {
            t.join(timeout);
        }
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
