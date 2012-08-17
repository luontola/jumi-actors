// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import com.google.caliper.*;
import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;

import java.util.concurrent.*;

/**
 * Create a ring of actors and forward a message around the ring multiple times.
 * <p/>
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
        MultiThreadedActors actors = new MultiThreadedActors(
                executor,
                new DynamicEventizerProvider(),
                new CrashEarlyFailureHandler(),
                new NullMessageListener()
        );
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
