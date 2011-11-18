// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import org.junit.Test;

import java.util.*;

public class SingleThreadedActorsTest extends ActorsContract<SingleThreadedActors> {

    private final List<SingleThreadedActors> createdActors = new ArrayList<SingleThreadedActors>();

    protected SingleThreadedActors newActors(ListenerFactory<?>... factories) {
        SingleThreadedActors actors = new SingleThreadedActors(factories) {
            @Override
            protected void handleUncaughtException(Object source, Throwable uncaughtException) {
                // Rethrowing here would break the general contracts from ActorsContract,
                // even though by default it's best in unit tests to fail early.
                // TODO: do a similar thing also in MultiThreadedActorsTest, to avoid noise in test run logs?
            }
        };
        createdActors.add(actors);
        return actors;
    }

    protected void processEvents() {
        for (SingleThreadedActors actors : createdActors) {
            actors.processEventsUntilIdle();
        }
    }

    @Test
    public void uncaught_exceptions_from_workers_will_be_rethrown_to_the_caller_by_default() {
        SingleThreadedActors actors = new SingleThreadedActors(new DummyListenerFactory());

        actors.doStartUnattendedWorker(new Runnable() {
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
            public void onSomething(String parameter) {
                throw new RuntimeException("dummy exception");
            }
        }, "DummyActor");
        actor.onSomething("");

        thrown.expect(Error.class);
        thrown.expectMessage("uncaught exception");
        actors.processEventsUntilIdle();
    }
}
