// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;

import javax.annotation.concurrent.*;
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
        return new TestRunExecutor(getProxyToSelf());
    }

    private ExecutorListener getProxyToSelf() {
        return actors.createSecondaryActor(ExecutorListener.class, new MyPrivateExecutorListener());
    }


    /**
     * Keeps the methods of {@link ExecutorListener} private to this {@link TestRunSpawner}.
     */
    @NotThreadSafe
    private class MyPrivateExecutorListener implements ExecutorListener {

        public void onCommandQueued(final Runnable runnable) {
            final ExecutorListener self = getProxyToSelf();

            @NotThreadSafe
            class OnCommandFinished implements Runnable {
                public void run() {
                    try {
                        runnable.run();
                    } finally {
                        self.onCommandFinished();
                    }
                }
            }
            workerCounter.fireWorkerStarted();
            realExecutor.execute(new OnCommandFinished());
        }

        public void onCommandFinished() {
            workerCounter.fireWorkerFinished();
        }
    }

    @ThreadSafe
    private static class TestRunExecutor implements Executor {
        private final ExecutorListener listener;

        private TestRunExecutor(ExecutorListener listener) {
            this.listener = listener;
        }

        public void execute(Runnable command) {
            listener.onCommandQueued(command);
        }
    }
}
