// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SingleThreadedActorsTest extends ActorsContract<SingleThreadedActors> {

    private final List<SingleThreadedActors> createdActors = new ArrayList<SingleThreadedActors>();

    @Override
    protected SingleThreadedActors newActors(ListenerFactory<?>... factories) {
        SingleThreadedActors actors = new SingleThreadedActors(factories) {
            @Override
            protected void handleUncaughtException(Object source, Throwable uncaughtException) {
                // Rethrowing here would break the general contracts from ActorsContract,
                // even though by default it's best in unit tests to fail early.
            }
        };
        createdActors.add(actors);
        return actors;
    }

    @Override
    protected void processEvents() {
        for (SingleThreadedActors actors : createdActors) {
            actors.processEventsUntilIdle();
        }
    }

    @Test
    public void uncaught_exceptions_from_workers_will_be_rethrown_to_the_caller_by_default() {
        SingleThreadedActors actors = new SingleThreadedActors(new DummyListenerFactory());

        actors.doStartUnattendedWorker(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("dummy exception");
            }
        });

        thrown.expect(Error.class);
        thrown.expectMessage("uncaught exception");
        actors.processEventsUntilIdle();
    }

    @Test
    public void uncaught_exceptions_from_pollers_will_be_rethrown_to_the_caller_by_default() {
        SingleThreadedActors actors = new SingleThreadedActors(new DummyListenerFactory());

        DummyListener actor = actors.createPrimaryActor(DummyListener.class, new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                throw new RuntimeException("dummy exception");
            }
        }, "DummyActor");
        actor.onSomething("");

        thrown.expect(Error.class);
        thrown.expectMessage("uncaught exception");
        actors.processEventsUntilIdle();
    }

    @Test
    public void will_keep_on_processing_messages_when_uncaught_exceptions_from_pollers_are_suppressed() {
        final List<Throwable> uncaughtExceptions = new ArrayList<Throwable>();
        SingleThreadedActors actors = new SingleThreadedActors(new DummyListenerFactory()) {
            @Override
            protected void handleUncaughtException(Object source, Throwable uncaughtException) {
                uncaughtExceptions.add(uncaughtException);
            }
        };

        DummyListener actor = actors.createPrimaryActor(DummyListener.class, new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                throw new RuntimeException("dummy exception");
            }
        }, "DummyActor");
        actor.onSomething("one");
        actor.onSomething("two");
        actors.processEventsUntilIdle();

        assertThat("uncaught exceptions count", uncaughtExceptions.size(), is(2));
    }

    @Test
    public void will_keep_on_processing_messages_when_uncaught_exceptions_from_workers_are_suppressed_and_workers_launch_more_workers() {
        final List<Throwable> uncaughtExceptions = new ArrayList<Throwable>();
        final SingleThreadedActors actors = new SingleThreadedActors(new DummyListenerFactory()) {
            @Override
            protected void handleUncaughtException(Object source, Throwable uncaughtException) {
                uncaughtExceptions.add(uncaughtException);
            }
        };

        final Runnable worker2 = new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("dummy exception");
            }
        };
        Runnable worker1 = new Runnable() {
            @Override
            public void run() {
                actors.doStartUnattendedWorker(worker2);
                throw new RuntimeException("dummy exception");
            }
        };
        actors.doStartUnattendedWorker(worker1);
        actors.processEventsUntilIdle();

        assertThat("uncaught exceptions count", uncaughtExceptions.size(), is(2));
    }

    @Test
    public void provides_an_asynchronous_executor() {
        final StringBuilder spy = new StringBuilder();
        SingleThreadedActors actors = new SingleThreadedActors(new DummyListenerFactory());

        Executor executor = actors.getExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                spy.append("a");
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                spy.append("b");
            }
        });

        assertThat("should not have executed synchronously", spy.toString(), is(""));
        actors.processEventsUntilIdle();
        assertThat("should have executed all Runnables", spy.toString(), is("ab"));
    }
}
