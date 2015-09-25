// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.examples;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executor;

import static fi.jumi.actors.examples.Pi.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PiTest {

    /**
     * An example of a unit test. We don't need to create the actors container, we just
     * need to wrap our test double into an ActorRef (never do this in production code!)
     */
    @Test
    public void each_worker_calculates_one_part_of_the_pi_approximation() {
        final List<Double> results = new ArrayList<>();
        ActorRef<ResultListener> listener = ActorRef.wrap((ResultListener) new ResultListener() {
            @Override
            public void onResult(double result) {
                results.add(result);
            }
        });

        int nrOfElements = 10;
        new Worker(0, nrOfElements, listener).run();
        new Worker(1, nrOfElements, listener).run();
        new Worker(2, nrOfElements, listener).run();

        assertThat(results.size(), is(3));
        assertThat(results.get(0), is(closeTo(3.041, 0.001)));
        assertThat(results.get(1), is(closeTo(0.049, 0.001)));
        assertThat(results.get(2), is(closeTo(0.016, 0.001)));
    }

    /**
     * An example of an integration test. We use a single-threaded actors container
     * so we can test easily, deterministically, how the actors play together.
     */
    @Test
    public void master_combines_the_results_from_workers() {
        class SpyListener implements ResultListener {
            double result;

            @Override
            public void onResult(double result) {
                this.result = result;
            }
        }
        SpyListener spy = new SpyListener();
        SingleThreadedActors actors = new SingleThreadedActors(
                new DynamicEventizerProvider(),
                // Will rethrow any exceptions to this test thread
                new CrashEarlyFailureHandler(),
                new NullMessageListener()
        );
        Executor workersThreadPool = actors.getExecutor(); // Also single-threaded, runs in this same test thread
        ActorThread thread = actors.startActorThread();

        ActorRef<Calculator> master = thread.bindActor(Calculator.class, new Master(thread, workersThreadPool, 10, 10));
        ActorRef<ResultListener> listener = thread.bindActor(ResultListener.class, spy);
        master.tell().approximatePi(listener);

        actors.processEventsUntilIdle(); // Any exceptions from actors would be thrown here
        assertThat(spy.result, is(closeTo(3.14, 0.01)));
    }
}
