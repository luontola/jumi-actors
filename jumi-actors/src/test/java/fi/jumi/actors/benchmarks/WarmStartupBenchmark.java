// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import com.google.caliper.SimpleBenchmark;
import fi.jumi.actors.*;

import java.util.concurrent.*;

public class WarmStartupBenchmark extends SimpleBenchmark {

    private final CyclicBarrier barrier = new CyclicBarrier(2);

    public void timeMultiThreadedActors(int reps) throws Exception {
        for (int i = 0; i < reps; i++) {
            ExecutorService executor = Executors.newCachedThreadPool();
            MultiThreadedActors actors = Util.createMultiThreadedActors(executor);
            ActorThread actorThread = actors.startActorThread();

            ActorRef<Runnable> runnable = actorThread.bindActor(Runnable.class, new Runnable() {
                @Override
                public void run() {
                    Util.sync(barrier);
                }
            });
            runnable.tell().run();
            Util.sync(barrier);

            executor.shutdownNow();
        }
    }

    public void timeSingleThreadedActors(int reps) {
        for (int i = 0; i < reps; i++) {
            SingleThreadedActors actors = Util.createSingleThreadedActors();
            ActorThread actorThread = actors.startActorThread();

            ActorRef<Runnable> runnable = actorThread.bindActor(Runnable.class, new Runnable() {
                @Override
                public void run() {
                }
            });
            runnable.tell().run();

            actors.processEventsUntilIdle();
        }
    }
}
