// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import fi.jumi.actors.ActorRef;

import javax.annotation.concurrent.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
public class WorkerCounter implements Executor {

    private final Executor realExecutor;
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    @GuardedBy("this")
    private ActorRef<WorkerListener> onFinished;

    public WorkerCounter(Executor realExecutor) {
        this.realExecutor = realExecutor;
    }

    @Override
    public void execute(Runnable command) {
        realExecutor.execute(new Worker(command));
    }

    public synchronized void afterPreviousWorkersFinished(ActorRef<WorkerListener> onFinished) {
        if (this.onFinished != null) {
            throw new IllegalStateException("a callback already exists; wait for the workers to finish before setting a new callback");
        }
        this.onFinished = onFinished;
        if (activeWorkers.get() == 0) {
            fireAllWorkersFinished();
        }
    }

    private synchronized void fireAllWorkersFinished() {
        if (onFinished != null) {
            onFinished.tell().onAllWorkersFinished();
            onFinished = null;
        }
    }


    // Used only from the Worker class, to make sure that they are always called

    private void fireWorkerCreated() {
        activeWorkers.incrementAndGet();
    }

    private void fireWorkerFinished() {
        int workers = activeWorkers.decrementAndGet();
        if (workers == 0) {
            fireAllWorkersFinished();
        }
    }

    @ThreadSafe
    private class Worker implements Runnable {
        private final Runnable command;

        public Worker(Runnable command) {
            fireWorkerCreated();
            this.command = command;
        }

        @Override
        public void run() {
            try {
                command.run();
            } finally {
                fireWorkerFinished();
            }
        }

        @Override
        public String toString() {
            return command.toString();
        }
    }
}
