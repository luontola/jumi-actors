// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.core.actors.Actors;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerCountingExecutor implements Executor {

    private final WorkersListener listener;
    private final Actors actors;
    private final Executor executor;

    // TODO: get rid of shared state once Actors and Executor are combined 
    private final AtomicInteger workers = new AtomicInteger();

    public WorkerCountingExecutor(WorkersListener listener, Actors actors, Executor executor) {
        this.actors = actors;
        this.executor = executor;
        this.listener = listener;
    }

    public void execute(final Runnable worker) {
        workers.incrementAndGet();
        executor.execute(new Runnable() {
            public void run() {
                try {
                    worker.run();
                } finally {
                    if (workers.decrementAndGet() == 0) {
                        listener.onAllWorkersFinished();
                    }
                }
            }
        });
    }
}
