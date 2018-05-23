// Copyright © 2011-2018, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import com.google.caliper.Benchmark;
import com.google.caliper.api.VmOptions;
import com.google.caliper.runner.CaliperMain;
import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;

import java.util.concurrent.*;

/**
 * Create the actors container, send and receive one message.
 */
// XXX: workaround for https://stackoverflow.com/questions/29199509/caliper-error-cicompilercount-of-1-is-invalid-must-be-at-least-2
@VmOptions("-XX:-TieredCompilation")
public class WarmStartupBenchmark {

    private final BusyWaitBarrier barrier = new BusyWaitBarrier();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Benchmark
    public void timeMultiThreadedActors(int reps) throws Exception {
        for (int i = 0; i < reps; i++) {
            MultiThreadedActors actors = new MultiThreadedActors(
                    executor,
                    new DynamicEventizerProvider(),
                    new CrashEarlyFailureHandler(),
                    new NullMessageListener()
            );
            ActorThread actorThread = actors.startActorThread();

            ActorRef<Runnable> runnable = actorThread.bindActor(Runnable.class, new Runnable() {
                @Override
                public void run() {
                    barrier.trigger();
                }
            });
            runnable.tell().run();
            barrier.await();

            actorThread.stop();
        }
    }

    @Benchmark
    public void timeSingleThreadedActors(int reps) {
        for (int i = 0; i < reps; i++) {
            SingleThreadedActors actors = new SingleThreadedActors(
                    new DynamicEventizerProvider(),
                    new CrashEarlyFailureHandler(),
                    new NullMessageListener()
            );
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

    public static void main(String[] args) {
        CaliperMain.main(WarmStartupBenchmark.class, new String[0]);
    }
}
