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

    public SingleThreadedActors(Eventizer<?>... factories) {
        super(factories);
    }

    @Override
    protected <T> void startEventPoller(String name, MessageQueue<Event<T>> queue, MessageSender<Event<T>> receiver) {
        pollers.add(new EventPoller<T>(queue, receiver));
    }

    @Override
    protected void doStartUnattendedWorker(Runnable worker) {
        workers.add(worker);
    }

    public void processEventsUntilIdle() {
        boolean idle;
        do {
            idle = true;
            for (Processable processable : getProcessableEvents()) {
                try {
                    if (processable.processedSomething()) {
                        idle = false;
                    }
                } catch (Throwable t) {
                    idle = false;
                    handleUncaughtException(processable, t);
                }
            }
        } while (!idle);
    }

    private List<Processable> getProcessableEvents() {
        List<Processable> results = new ArrayList<Processable>();
        results.addAll(pollers);
        for (Runnable runnable : takeAll(workers)) {
            results.add(new ProcessableRunnable(runnable));
        }
        return results;
    }

    private static ArrayList<Runnable> takeAll(List<Runnable> list) {
        ArrayList<Runnable> copy = new ArrayList<Runnable>(list);
        list.clear();
        return copy;
    }

    protected void handleUncaughtException(Object source, Throwable uncaughtException) {
        throw new Error("uncaught exception from " + source, uncaughtException);
    }

    public Executor getExecutor() {
        return new AsynchronousExecutor();
    }


    private interface Processable {

        boolean processedSomething();
    }

    @NotThreadSafe
    private static class ProcessableRunnable implements Processable {
        private final Runnable runnable;

        public ProcessableRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public boolean processedSomething() {
            runnable.run();
            return true;
        }
    }

    @ThreadSafe
    private class EventPoller<T> implements Processable {

        private final MessageQueue<Event<T>> source;
        private final MessageSender<Event<T>> target;

        public EventPoller(MessageQueue<Event<T>> source, MessageSender<Event<T>> target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean processedSomething() {
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
        @Override
        public void execute(Runnable command) {
            workers.add(command);
        }
    }
}
