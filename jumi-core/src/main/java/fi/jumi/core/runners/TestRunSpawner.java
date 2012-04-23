// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class TestRunSpawner {

    private final ActorThread actorThread;
    private final WorkerCounter workerCounter;
    private final Executor realExecutor;

    public TestRunSpawner(ActorThread actorThread, WorkerCounter workerCounter, Executor realExecutor) {
        this.actorThread = actorThread;
        this.workerCounter = workerCounter;
        this.realExecutor = realExecutor;
    }

    public Executor getExecutor() {
        return self().tell();
    }

    private ActorRef<ExecutorListener> self() {
        return actorThread.createActor(ExecutorListener.class, new WorkerCountingExecutor(workerCounter, realExecutor));
    }


    /**
     * Keeps the methods of {@link ExecutorListener} private to this {@link TestRunSpawner}.
     */
    @NotThreadSafe
    private class WorkerCountingExecutor implements ExecutorListener {

        private final WorkerCounter workerCounter;
        private final Executor realExecutor;

        public WorkerCountingExecutor(WorkerCounter workerCounter, Executor realExecutor) {
            this.workerCounter = workerCounter;
            this.realExecutor = realExecutor;
        }

        @Override
        public void execute(final Runnable runnable) {
            final ActorRef<ExecutorListener> self = self();

            @NotThreadSafe
            class RunWorkerAndNotifyWhenFinished implements Runnable {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } finally {
                        self.tell().onCommandFinished();
                    }
                }
            }
            workerCounter.fireWorkerStarted();
            realExecutor.execute(new RunWorkerAndNotifyWhenFinished());
        }

        @Override
        public void onCommandFinished() {
            workerCounter.fireWorkerFinished();
        }
    }
}
