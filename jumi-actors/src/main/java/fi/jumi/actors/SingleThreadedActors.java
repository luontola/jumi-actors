// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import javax.annotation.concurrent.*;
import java.util.*;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SingleThreadedActors extends Actors {

    private final List<EventPoller<?>> pollers = new ArrayList<EventPoller<?>>();
    private final List<Runnable> workers = new ArrayList<Runnable>();

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
        // TODO: clean this messy method; maybe unify processing pollers and workers, removing the duplication in error handling
        boolean idle;
        do {
            idle = true;
            for (EventPoller<?> poller : pollers) {
                try {
                    if (poller.processMessage()) {
                        idle = false;
                    }
                } catch (Throwable t) {
                    idle = false;
                    handleUncaughtException(poller, t);
                }
            }
            for (Runnable worker : takeAll(workers)) {
                idle = false;
                try {
                    worker.run();
                } catch (Throwable t) {
                    handleUncaughtException(worker, t);
                }
            }
        } while (!idle);
    }

    protected void handleUncaughtException(Object source, Throwable uncaughtException) {
        throw new Error("uncaught exception from " + source, uncaughtException);
    }

    private static ArrayList<Runnable> takeAll(List<Runnable> list) {
        ArrayList<Runnable> copy = new ArrayList<Runnable>(list);
        list.clear();
        return copy;
    }

    public Executor getExecutor() {
        return new AsynchronousExecutor();
    }


    @ThreadSafe
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

    @NotThreadSafe
    private class AsynchronousExecutor implements Executor {
        public void execute(Runnable command) {
            workers.add(command);
        }
    }
}
