// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import java.util.*;
import java.util.concurrent.*;

public class MultiThreadedActors extends Actors {

    private final Set<Thread> actorThreads = Collections.synchronizedSet(new HashSet<Thread>());
    private final ExecutorService unattendedWorkers = Executors.newCachedThreadPool();

    public MultiThreadedActors(ListenerFactory<?>... factories) {
        super(factories);
    }

    protected <T> void startEventPoller(String name, MessageQueue<Event<T>> queue, MessageSender<Event<T>> receiver) {
        Thread t = new Thread(new ActorContext<T>(queue, new EventPoller<T>(queue, receiver)), name);
        t.start();
        actorThreads.add(t);
    }

    protected void doStartUnattendedWorker(Runnable worker) {
        unattendedWorkers.execute(worker);
    }

    public void shutdown(long timeout) throws InterruptedException {
        for (Thread t : actorThreads) {
            t.interrupt();
        }
        unattendedWorkers.shutdown();
        for (Thread t : actorThreads) {
            t.join(timeout);
        }
        unattendedWorkers.awaitTermination(timeout, TimeUnit.MILLISECONDS);
    }


    private static class EventPoller<T> implements Runnable {
        private final MessageReceiver<Event<T>> events;
        private final MessageSender<Event<T>> target;

        public EventPoller(MessageReceiver<Event<T>> events, MessageSender<Event<T>> target) {
            this.events = events;
            this.target = target;
        }

        public void run() {
            try {
                while (!Thread.interrupted()) {
                    Event<T> message = events.take();
                    target.send(message);
                }
            } catch (InterruptedException e) {
                // actor was told to exit
            }
        }
    }
}
