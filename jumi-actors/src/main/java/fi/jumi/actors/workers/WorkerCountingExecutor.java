// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Executor;

@ThreadSafe
public class WorkerCountingExecutor implements Executor {

    private final Executor realExecutor;
    private final WorkerCounter workerCounter;

    public WorkerCountingExecutor(Executor realExecutor, WorkerCounter workerCounter) {
        this.realExecutor = realExecutor;
        this.workerCounter = workerCounter;
    }

    @Override
    public void execute(Runnable command) {
        workerCounter.fireWorkerStarted();
        realExecutor.execute(new Worker(command));
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
                workerCounter.fireWorkerFinished();
            }
        }

        @Override
        public String toString() {
            return realCommand.toString();
        }
    }
}
