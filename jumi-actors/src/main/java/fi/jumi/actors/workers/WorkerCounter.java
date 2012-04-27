// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
public class WorkerCounter {

    private final AtomicInteger activeWorkers = new AtomicInteger(0);
    private final Runnable onFinished;

    public WorkerCounter(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void fireWorkerStarted() {
        activeWorkers.incrementAndGet();
    }

    public void fireWorkerFinished() {
        int workers = activeWorkers.decrementAndGet();
        if (workers == 0) {
            onFinished.run();
        }
    }
}
