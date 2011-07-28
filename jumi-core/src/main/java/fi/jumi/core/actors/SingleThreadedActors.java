// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.actors;

import java.util.*;

public class SingleThreadedActors extends Actors {

    private List<EventPoller<?>> pollers = new ArrayList<EventPoller<?>>();
    private List<Runnable> workers = new ArrayList<Runnable>();

    public SingleThreadedActors(ListenerFactory<?>... factories) {
        super(factories);
    }

    protected <T> void startEventPoller(String name, MessageQueue<Event<T>> queue, MessageSender<Event<T>> receiver) {
        pollers.add(new EventPoller<T>(queue, receiver));
    }

    protected void doStartUnattendedWorker(Runnable worker) {
        workers.add(worker);
    }

    public void processEventsUntilIdle() {
        boolean idle;
        do {
            idle = true;
            for (EventPoller<?> poller : pollers) {
                if (poller.processMessage()) {
                    idle = false;
                }
            }
            for (Runnable worker : workers) {
                try {
                    worker.run();
                } catch (Throwable t) {
                    // TODO: rethrow or send to a custom exception handler?
                    t.printStackTrace();
                }
                idle = false;
            }
            workers.clear();
        } while (!idle);
    }


    private class EventPoller<T> {

        private final MessageQueue<Event<T>> source;
        private final MessageSender<Event<T>> target;

        public EventPoller(MessageQueue<Event<T>> source, MessageSender<Event<T>> target) {
            this.source = source;
            this.target = target;
        }

        public boolean processMessage() {
            Event<T> event = source.poll();
            if (event != null) {
                initActorContext(source);
                try {
                    target.send(event);
                } finally {
                    clearActorContext();
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
