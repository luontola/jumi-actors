// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

@ThreadSafe
public class MonitoredExecutor implements Executor {

    private final ExecutorService realExecutor;
    private final WorkerCounter workerCounter;

    public MonitoredExecutor(ExecutorService realExecutor, WorkerCounter workerCounter) {
        this.realExecutor = realExecutor;
        this.workerCounter = workerCounter;
    }

    @Override
    public void execute(Runnable command) {
        realExecutor.execute(new Worker(command));
    }


    @ThreadSafe
    private class Worker implements Runnable {
        private final Runnable command;

        public Worker(Runnable command) {
            this.command = command;
        }

        @Override
        public void run() {
            workerCounter.fireWorkerStarted();
            try {
                command.run();
            } finally {
                workerCounter.fireWorkerFinished();
            }
        }
    }
}
