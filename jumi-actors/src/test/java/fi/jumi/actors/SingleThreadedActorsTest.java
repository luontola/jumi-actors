// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.eventizers.EventizerProvider;
import fi.jumi.actors.failures.*;
import fi.jumi.actors.logging.MessageLogger;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.*;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class SingleThreadedActorsTest extends ActorsContract<SingleThreadedActors> {

    private final List<SingleThreadedActors> createdActors = new ArrayList<SingleThreadedActors>();

    @Override
    protected SingleThreadedActors newActors(EventizerProvider eventizerProvider, FailureHandler failureHandler, MessageLogger logger) {
        SingleThreadedActors actors = new SingleThreadedActors(eventizerProvider, failureHandler, logger);
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
    public void with_CrashEarlyFailureHandler_will_rethrow_uncaught_exceptions_to_the_caller() {
        CrashEarlyFailureHandler failureHandler = new CrashEarlyFailureHandler();
        SingleThreadedActors actors = new SingleThreadedActors(defaultEventizerProvider, failureHandler, defaultLogger);

        ActorThread actorThread = actors.startActorThread();
        DummyListener rawActor = new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                throw new RuntimeException("dummy exception");
            }
        };
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, rawActor);
        actor.tell().onSomething("");

        try {
            actors.processEventsUntilIdle();
            fail("should have thrown an exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("uncaught exception from " + rawActor));
            assertThat(e.getCause().getMessage(), is("dummy exception"));
        }
    }

    @Test
    public void provides_an_asynchronous_executor() {
        final StringBuilder spy = new StringBuilder();
        SingleThreadedActors actors = new SingleThreadedActors(new DynamicEventizerProvider(), defaultFailureHandler, defaultLogger);

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

    @Test
    public void the_asynchronous_executor_is_logged_using_the_same_MessageLogger_as_the_actors() {
        Executor loggedExecutor = mock(Executor.class, "loggedExecutor");
        MessageLogger messageLogger = mock(MessageLogger.class);
        stub(messageLogger.getLoggedExecutor(Matchers.<Executor>any())).toReturn(loggedExecutor);

        SingleThreadedActors actors = new SingleThreadedActors(defaultEventizerProvider, defaultFailureHandler, messageLogger);
        Executor asynchronousExecutor = actors.getExecutor();

        assertThat(asynchronousExecutor, is(loggedExecutor));
    }
}
