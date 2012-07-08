// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import fi.jumi.actors.ActorRef;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
public class WorkerCounter implements Executor {

    private final AtomicInteger activeWorkers = new AtomicInteger(0);
    private final ActorRef<WorkerListener> onFinished;

    private final Executor realExecutor;
    private final List<Worker> initialWorkers = new ArrayList<Worker>();
    private volatile boolean initialWorkersStarted = false;

    public WorkerCounter(Executor realExecutor, ActorRef<WorkerListener> onFinished) {
        this.realExecutor = realExecutor;
        this.onFinished = onFinished;
    }

    @Override
    public void execute(Runnable realCommand) {
        startWorker(new Worker(realCommand));
    }

    private synchronized void startWorker(Worker worker) {
        fireWorkerCreated();
        if (!initialWorkersStarted) {
            initialWorkers.add(worker);
        } else {
            realExecutor.execute(worker);
        }
    }

    public synchronized void startInitialWorkers() {
        if (initialWorkersStarted) {
            throw new IllegalStateException("initial workers have already been started");
        }
        initialWorkersStarted = true;
        for (Worker initialWorker : initialWorkers) {
            realExecutor.execute(initialWorker);
        }
    }

    private void fireWorkerCreated() {
        activeWorkers.incrementAndGet();
    }

    private void fireWorkerFinished() {
        int workers = activeWorkers.decrementAndGet();
        if (workers == 0) {
            onFinished.tell().onAllWorkersFinished();
        }
    }

    @ThreadSafe
    private class Worker implements Runnable {
        private final Runnable realCommand;

        public Worker(Runnable realCommand) {
            this.realCommand = realCommand;
        }

        @Override
        public void run() {
            try {
                realCommand.run();
            } finally {
                fireWorkerFinished();
            }
        }

        @Override
        public String toString() {
            return realCommand.toString();
        }
    }
}
