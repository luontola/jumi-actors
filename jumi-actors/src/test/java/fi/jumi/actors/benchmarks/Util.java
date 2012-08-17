// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;

import java.util.concurrent.*;

class Util {

    private static final boolean DEBUG = false;

    public static MultiThreadedActors createMultiThreadedActors(ExecutorService executor) {
        return new MultiThreadedActors(
                executor,
                new DynamicEventizerProvider(),
                new CrashEarlyFailureHandler(),
                getMessageListener()
        );
    }

    public static SingleThreadedActors createSingleThreadedActors() {
        return new SingleThreadedActors(
                new DynamicEventizerProvider(),
                new CrashEarlyFailureHandler(),
                getMessageListener()
        );
    }

    private static MessageListener getMessageListener() {
        return DEBUG ? new PrintStreamMessageLogger(System.out) : new NullMessageListener();
    }

    public static void sync(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
