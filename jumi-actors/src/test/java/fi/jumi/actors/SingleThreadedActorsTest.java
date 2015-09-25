// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.EventizerProvider;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.*;
import java.util.concurrent.Executor;

import static fi.jumi.actors.Matchers.hasCause;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class SingleThreadedActorsTest extends ActorsContract<SingleThreadedActors> {

    private final List<SingleThreadedActors> createdActors = new ArrayList<>();

    @Override
    protected SingleThreadedActors newActors(EventizerProvider eventizerProvider, FailureHandler failureHandler, MessageListener messageListener) {
        SingleThreadedActors actors = new SingleThreadedActors(eventizerProvider, failureHandler, messageListener);
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
        SingleThreadedActors actors = new SingleThreadedActors(defaultEventizerProvider, failureHandler, defaultMessageListener);

        ActorThread actorThread = actors.startActorThread();
        DummyListener exceptionThrowingActor = new DummyListener() {
            @Override
            public void onSomething(String parameter) {
                throw new DummyException();
            }
        };
        ActorRef<DummyListener> actor = actorThread.bindActor(DummyListener.class, exceptionThrowingActor);
        actor.tell().onSomething("the message being processed");

        thrown.expect(hasCause(instanceOf(DummyException.class)));
        thrown.expectMessage("uncaught exception");
        thrown.expectMessage(exceptionThrowingActor.toString());
        thrown.expectMessage("the message being processed");
        actors.processEventsUntilIdle();
    }

    @Test
    public void provides_an_asynchronous_executor() {
        final StringBuilder spy = new StringBuilder();
        SingleThreadedActors actors = new SingleThreadedActors(new DynamicEventizerProvider(), defaultFailureHandler, defaultMessageListener);

        Executor executor = actors.getExecutor();
        executor.execute(() -> spy.append("a"));
        executor.execute(() -> spy.append("b"));

        assertThat("should not have executed synchronously", spy.toString(), is(""));
        actors.processEventsUntilIdle();
        assertThat("should have executed all Runnables", spy.toString(), is("ab"));
    }

    @Test
    public void the_asynchronous_executor_is_hooked_into_the_same_MessageListener_as_the_actors_use() {
        Executor listenedExecutor = mock(Executor.class, "listenedExecutor");
        MessageListener messageListener = mock(MessageListener.class);
        stub(messageListener.getListenedExecutor(Matchers.<Executor>any())).toReturn(listenedExecutor);

        SingleThreadedActors actors = new SingleThreadedActors(defaultEventizerProvider, defaultFailureHandler, messageListener);
        Executor asynchronousExecutor = actors.getExecutor();

        assertThat(asynchronousExecutor, is(listenedExecutor));
    }
}
