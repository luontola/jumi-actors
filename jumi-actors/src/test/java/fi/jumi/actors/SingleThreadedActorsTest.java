// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.*;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SingleThreadedActorsTest extends ActorsContract<SingleThreadedActors> {

    private final List<SingleThreadedActors> createdActors = new ArrayList<SingleThreadedActors>();

    @Override
    protected SingleThreadedActors newActors(EventizerLocator eventizerLocator) {
        SingleThreadedActors actors = new SingleThreadedActors(eventizerLocator);
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
    public void uncaught_exceptions_from_actors_will_be_rethrown_to_the_caller_by_default() {
        SingleThreadedActors actors = new SingleThreadedActors(new ComposedEventizerLocator(new DummyListenerEventizer()));

        ActorThread actorThread = actors.startActorThread("DummyActor");
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                throw new RuntimeException("dummy exception");
            }
        });
        actor.tell().onSomething("");

        thrown.expect(Error.class);
        thrown.expectMessage("uncaught exception");
        actors.processEventsUntilIdle();
    }

    @Test
    public void will_keep_on_processing_messages_when_uncaught_exceptions_from_actors_are_suppressed() {
        final List<Throwable> uncaughtExceptions = new ArrayList<Throwable>();
        SingleThreadedActors actors = new SingleThreadedActors(new ComposedEventizerLocator(new DummyListenerEventizer())) {
            @Override
            protected void handleUncaughtException(Object source, Throwable uncaughtException) {
                uncaughtExceptions.add(uncaughtException);
            }
        };

        ActorThread actorThread = actors.startActorThread("DummyActor");
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                throw new RuntimeException("dummy exception");
            }
        });
        actor.tell().onSomething("one");
        actor.tell().onSomething("two");
        actors.processEventsUntilIdle();

        assertThat("uncaught exceptions count", uncaughtExceptions.size(), is(2));
    }

    @Test
    public void provides_an_asynchronous_executor() {
        final StringBuilder spy = new StringBuilder();
        SingleThreadedActors actors = new SingleThreadedActors(new ComposedEventizerLocator(new DummyListenerEventizer()));

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
