// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import fi.jumi.actors.ActorRef;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
public class WorkerCounter {

    private final AtomicInteger activeWorkers = new AtomicInteger(0);
    private final ActorRef<Runnable> onFinished;

    public WorkerCounter(ActorRef<Runnable> onFinished) {
        this.onFinished = onFinished;
    }

    // The following methods should be called only from the WorkerCountingExecutor class

    void fireWorkerStarted() {
        activeWorkers.incrementAndGet();
    }

    void fireWorkerFinished() {
        int workers = activeWorkers.decrementAndGet();
        if (workers == 0) {
            onFinished.tell().run();
        }
    }
}
