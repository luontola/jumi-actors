// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import com.google.caliper.*;
import fi.jumi.actors.*;

import java.util.concurrent.*;

/**
 * Based on http://blog.grayproductions.net/articles/erlang_message_passing/
 */
public class RingBenchmark extends SimpleBenchmark {

    @Param int ringSize;
    @Param int roundTrips;

    private final CyclicBarrier barrier = new CyclicBarrier(2);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private ActorRef<Ring> ring;

    @Override
    protected void setUp() throws Exception {
        MultiThreadedActors actors = Factory.createMultiThreadedActors(executor);
        ActorThread actorThread = actors.startActorThread();

        ring = actorThread.bindActor(Ring.class, new RingStart(actorThread) {
            @Override
            public void forward(int roundTrips) {
                super.forward(roundTrips);
                if (roundTrips == 0) {
                    sync(barrier);
                }
            }
        });
        ring.tell().build(ringSize, ring);
    }

    @Override
    protected void tearDown() throws Exception {
        executor.shutdownNow();
    }

    public void timeRingRoundTrips(int reps) {
        for (int i = 0; i < reps; i++) {
            ring.tell().forward(roundTrips);
            sync(barrier);
        }
    }

    public static void sync(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
