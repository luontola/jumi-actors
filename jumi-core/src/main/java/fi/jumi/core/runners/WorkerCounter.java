// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class WorkerCounter {

    private final WorkerCounterListener listener;
    private int workers = 0;

    public WorkerCounter(WorkerCounterListener listener) {
        this.listener = listener;
    }

    public void fireWorkerStarted() {
        workers++;
    }

    public void fireWorkerFinished() {
        workers--;
        assert workers >= 0;
        if (workers == 0) {
            listener.onAllWorkersFinished();
        }
    }
}
