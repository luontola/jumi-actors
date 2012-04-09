// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class TestRunSpawner {

    private final OnDemandActors actors;
    private final Executor realExecutor;
    private final WorkerCounter workerCounter;

    public TestRunSpawner(WorkerCounter workerCounter, OnDemandActors actors, Executor realExecutor) {
        this.actors = actors;
        this.realExecutor = realExecutor;
        this.workerCounter = workerCounter;
    }

    public Executor getExecutor() {
        return getProxyToSelf();
    }

    private ExecutorListener getProxyToSelf() {
        return actors.createSecondaryActor(ExecutorListener.class, new WorkerCountingExecutor(workerCounter, realExecutor));
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
            final ExecutorListener self = getProxyToSelf();

            @NotThreadSafe
            class RunWorkerAndNotifyWhenFinished implements Runnable {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } finally {
                        self.onCommandFinished();
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
