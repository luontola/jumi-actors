// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.examples;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.EventizerProvider;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;

import java.util.concurrent.*;

/**
 * Comparison of Jumi Actors and Akka Actors, by implementing the same algorithm for calculating Pi as in <a
 * href="http://doc.akka.io/docs/akka/2.0.2/intro/getting-started-first-java.html">Akka's getting started tutorial</a>.
 * You may read that first and then compare how the style, performance etc. of this actor library differs from it.
 */
public class Pi {

    // If you enabled logging, it might be good to reduce the number of messages
    // to something small (e.g. 10-1000) to make the logs easier to read.

    public static final boolean LOGGING = false;
    public static final int NR_OF_WORKERS = 4;
    public static final int NR_OF_MESSAGES = 10000;
    public static final int NR_OF_ELEMENTS = 10000;

    public static void main(String[] args) {

        // *** CONFIGURE THE ACTORS CONTAINER ***

        // Executors for creating actor threads and executing the Pi calculating workers
        final ExecutorService actorsThreadPool = Executors.newCachedThreadPool();
        final ExecutorService workersThreadPoolImpl = Executors.newFixedThreadPool(NR_OF_WORKERS);

        // Log all messages to show how the system works
        MessageListener messageListener = LOGGING ?
                new PrintStreamMessageLogger(System.out) :
                new NullMessageListener(); // no logging - the recommended setting in production

        // Also log what commands the actors execute using this executor
        Executor workersThreadPool = messageListener.getListenedExecutor(workersThreadPoolImpl);

        // On failure, log the error and keep on processing more messages
        FailureHandler failureHandler = new PrintStreamFailureLogger(System.out);

        // Supports all actor interfaces using reflection
        EventizerProvider eventizerProvider = new DynamicEventizerProvider();

        // Multi-threaded actors implementation for production use
        MultiThreadedActors actors = new MultiThreadedActors(actorsThreadPool, eventizerProvider, failureHandler, messageListener);


        // *** START UP THE APPLICATION ***

        ActorThread actorThread = actors.startActorThread();

        ActorRef<Calculator> master = actorThread.bindActor(Calculator.class,
                new Master(actorThread, workersThreadPool, NR_OF_MESSAGES, NR_OF_ELEMENTS));

        ActorRef<ResultListener> listener = actorThread.bindActor(ResultListener.class, new ResultListener() {
            private final long start = System.currentTimeMillis();

            @Override
            public void onResult(double pi) {
                long end = System.currentTimeMillis();
                long duration = (end - start);
                System.out.println("Pi approximation: " + pi);
                System.out.println("Calculation time: " + duration + " ms");

                // Stop all actor threads and workers immediately
                actorsThreadPool.shutdownNow();
                workersThreadPoolImpl.shutdownNow();
            }
        });

        // Start the calculation
        master.tell().approximatePi(listener);
    }

    public static class Master implements Calculator {

        private final ActorThread currentThread;
        private final Executor workersThreadPool;

        private final int nrOfMessages;
        private final int nrOfElements;

        public Master(ActorThread currentThread, Executor workersThreadPool, int nrOfMessages, int nrOfElements) {
            this.currentThread = currentThread;
            this.workersThreadPool = workersThreadPool;
            this.nrOfMessages = nrOfMessages;
            this.nrOfElements = nrOfElements;
        }

        @Override
        public void approximatePi(final ActorRef<ResultListener> listener) {
            class Reducer implements ResultListener {
                private double pi = 0.0;
                private int nrOfResults = 0;

                @Override
                public void onResult(double partialPi) {
                    pi += partialPi;
                    nrOfResults++;
                    if (nrOfResults == nrOfMessages) {
                        listener.tell().onResult(pi);
                    }
                }
            }

            ActorRef<ResultListener> reducer = currentThread.bindActor(ResultListener.class, new Reducer());

            for (int start = 0; start < nrOfMessages; start++) {
                workersThreadPool.execute(new Worker(start, nrOfElements, reducer));
            }
        }
    }

    public static class Worker implements Runnable {

        private final int start;
        private final int nrOfElements;
        private final ActorRef<ResultListener> listener;

        public Worker(int start, int nrOfElements, ActorRef<ResultListener> listener) {
            this.start = start;
            this.nrOfElements = nrOfElements;
            this.listener = listener;
        }

        @Override
        public void run() {
            double result = calculate();
            listener.tell().onResult(result);
        }

        private double calculate() {
            double acc = 0.0;
            for (int i = start * nrOfElements; i <= ((start + 1) * nrOfElements - 1); i++) {
                acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
            }
            return acc;
        }
    }

    public interface Calculator {
        void approximatePi(ActorRef<ResultListener> listener);
    }

    public interface ResultListener {
        void onResult(double result);
    }
}
