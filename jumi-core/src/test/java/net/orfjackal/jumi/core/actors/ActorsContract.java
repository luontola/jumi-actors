// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import net.orfjackal.jumi.core.dynamicevents.DynamicListenerFactory;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public abstract class ActorsContract {

    private static final long TIMEOUT = 1000;

    private Actors actors;
    private final Queue<String> events = new ConcurrentLinkedQueue<String>();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void initActors() {
        actors = newActors(new DummyListenerFactory(), new DynamicListenerFactory<Runnable>(Runnable.class));
    }

    protected abstract Actors newActors(ListenerFactory<?>... factories);

    protected abstract void processEvents();

    protected void logEvent(String event) {
        events.add(event);
    }

    protected void awaitForEvents(int expectedEventCount) {
        processEvents();

        long limit = System.currentTimeMillis() + TIMEOUT;
        while (events.size() < expectedEventCount && System.currentTimeMillis() < limit) {
            Thread.yield();
        }
    }

    protected void assertLoggedEvents(String... expected) {
        List<String> actual = new ArrayList<String>(events);
        assertThat("events", actual, is(Arrays.asList(expected)));
    }


    // normal event-polling actors

    @Test
    public void method_calls_on_handle_are_forwarded_to_target() throws InterruptedException {
        LinkedBlockingQueue<String> spy = new LinkedBlockingQueue<String>();
        DummyListener handle = actors.startEventPoller(DummyListener.class, new EventParameterSpy(spy), "ActorName");

        handle.onSomething("event parameter");
        processEvents();

        String parameter = spy.poll(TIMEOUT, TimeUnit.MILLISECONDS);
        assertThat(parameter, is("event parameter"));
    }

    @Test
    public void actor_processes_multiple_events_in_the_order_they_were_sent() throws InterruptedException {
        LinkedBlockingQueue<String> spy = new LinkedBlockingQueue<String>();
        DummyListener handle = actors.startEventPoller(DummyListener.class, new EventParameterSpy(spy), "ActorName");

        handle.onSomething("event 1");
        handle.onSomething("event 2");
        handle.onSomething("event 3");
        processEvents();

        List<String> receivedEvents = new ArrayList<String>();
        receivedEvents.add(spy.poll(TIMEOUT, TimeUnit.MILLISECONDS));
        receivedEvents.add(spy.poll(TIMEOUT, TimeUnit.MILLISECONDS));
        receivedEvents.add(spy.poll(TIMEOUT, TimeUnit.MILLISECONDS));
        assertThat(receivedEvents, is(Arrays.asList("event 1", "event 2", "event 3")));
    }


    // secondary interfaces

    @Test
    public void an_actor_can_receive_events_in_the_same_thread_through_a_secondary_interface() {
        final Actors actors = newActors(DynamicListenerFactory.factoriesFor(PrimaryInterface.class, SecondaryInterface.class));
        final AtomicReference<SecondaryInterface> secondaryHandleRef = new AtomicReference<SecondaryInterface>();
        final AtomicReference<Thread> primaryEventThread = new AtomicReference<Thread>();
        final AtomicReference<Thread> secondaryEventThread = new AtomicReference<Thread>();

        class MultiPurposeActor implements PrimaryInterface, SecondaryInterface {
            public void onPrimaryEvent() {
                // binding must be done inside the actor
                secondaryHandleRef.set(actors.bindSecondaryInterface(SecondaryInterface.class, this));

                primaryEventThread.set(Thread.currentThread());
                logEvent("primary event");
            }

            public void onSecondaryEvent() {
                secondaryEventThread.set(Thread.currentThread());
                logEvent("secondary event");
            }
        }


        MultiPurposeActor actor = new MultiPurposeActor();

        PrimaryInterface primaryHandle = actors.startEventPoller(PrimaryInterface.class, actor, "ActorName");
        primaryHandle.onPrimaryEvent();
        awaitForEvents(1);

        SecondaryInterface secondaryHandle = secondaryHandleRef.get();
        secondaryHandle.onSecondaryEvent();
        awaitForEvents(2);

        assertThat("events", events, contains("primary event", "secondary event"));
        assertThat("secondary event happened", secondaryEventThread.get(), is(notNullValue()));
        assertThat("secondary event happened in same thread as primary event", secondaryEventThread.get(), is(primaryEventThread.get()));
    }


    @Test
    public void secondary_interfaces_cannot_be_bound_outside_an_actor() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("not inside an actor");

        actors.bindSecondaryInterface(DummyListener.class, mock(DummyListener.class));
    }


    // unattended workers

    @Test
    public void when_worker_finishes_the_actor_which_started_it_is_notified_in_the_actor_thread() throws InterruptedException {
        final AtomicReference<Thread> actorThread = new AtomicReference<Thread>();
        final AtomicReference<Thread> onFinishedThread = new AtomicReference<Thread>();

        final Runnable worker = new Runnable() {
            public void run() {
                logEvent("run worker");
            }
        };
        final Runnable onFinished = new Runnable() {
            public void run() {
                onFinishedThread.set(Thread.currentThread());
                logEvent("on finished");
            }
        };
        actors.startEventPoller(Runnable.class, new Runnable() {
            public void run() {
                actorThread.set(Thread.currentThread());
                logEvent("start worker");
                actors.startUnattendedWorker(worker, onFinished);
            }
        }, "Actor").run();
        awaitForEvents(3);

        assertLoggedEvents("start worker", "run worker", "on finished");
        assertThat("notification should have been in the actor thread", onFinishedThread.get(), is(actorThread.get()));
    }

    @Test
    public void the_actor_is_notified_even_if_the_worker_throws_an_exception() throws InterruptedException {
        final BlockingQueue<String> events = new LinkedBlockingQueue<String>();

        final Runnable worker = new Runnable() {
            public void run() {
                events.add("run worker");
                // ThreadDeath is not printed when it's thrown, so this keeps the test logs cleaner
                throw new ThreadDeath();
            }
        };
        final Runnable onFinished = new Runnable() {
            public void run() {
                events.add("on finished");
            }
        };
        actors.startEventPoller(Runnable.class, new Runnable() {
            public void run() {
                events.add("start worker");
                actors.startUnattendedWorker(worker, onFinished);
            }
        }, "Actor").run();
        processEvents();

        assertThat("event 1", events.poll(TIMEOUT, TimeUnit.MILLISECONDS), is("start worker"));
        assertThat("event 2", events.poll(TIMEOUT, TimeUnit.MILLISECONDS), is("run worker"));
        assertThat("event 3", events.poll(TIMEOUT, TimeUnit.MILLISECONDS), is("on finished"));
        assertThat("no more events", events.poll(), is(nullValue()));
    }

    @Test
    public void unattended_workers_cannot_be_bound_outside_an_actor() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("not inside an actor");

        actors.startUnattendedWorker(new DummyRunnable(), new DummyRunnable());
    }


    // setup

    @Test
    public void listener_factories_must_be_registered_for_them_to_be_usable() {
        NoFactoryForThisListener listener = new NoFactoryForThisListener() {
        };

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported listener type");
        thrown.expectMessage(NoFactoryForThisListener.class.getName());

        actors.startEventPoller(NoFactoryForThisListener.class, listener, "ActorName");
    }

    // TODO: single-threaded actor manager for unit tests


    private static class EventParameterSpy implements DummyListener {
        private final LinkedBlockingQueue<String> spy;

        public EventParameterSpy(LinkedBlockingQueue<String> spy) {
            this.spy = spy;
        }

        public void onSomething(String parameter) {
            spy.offer(parameter);
        }
    }

    protected static class CurrentThreadSpy implements DummyListener {
        private final LinkedBlockingQueue<Thread> spy;

        public CurrentThreadSpy(LinkedBlockingQueue<Thread> spy) {
            this.spy = spy;
        }

        public void onSomething(String parameter) {
            spy.offer(Thread.currentThread());
        }
    }

    private interface PrimaryInterface {
        void onPrimaryEvent();
    }

    private interface SecondaryInterface {
        void onSecondaryEvent();
    }


    // test data

    private interface NoFactoryForThisListener {
    }

    protected interface DummyListener {
        void onSomething(String parameter);
    }

    protected class DummyListenerFactory implements ListenerFactory<DummyListener> {

        public Class<DummyListener> getType() {
            return DummyListener.class;
        }

        public DummyListener newFrontend(MessageSender<Event<DummyListener>> target) {
            return new DummyListenerToEvent(target);
        }

        public MessageSender<Event<DummyListener>> newBackend(DummyListener target) {
            return new EventToDummyListener(target);
        }
    }

    private class OnSomethingEvent implements Event<DummyListener> {
        private final String parameter;

        public OnSomethingEvent(String parameter) {
            this.parameter = parameter;
        }

        public void fireOn(DummyListener target) {
            target.onSomething(parameter);
        }
    }

    private class DummyListenerToEvent implements DummyListener {
        private final MessageSender<Event<DummyListener>> sender;

        public DummyListenerToEvent(MessageSender<Event<DummyListener>> sender) {
            this.sender = sender;
        }

        public void onSomething(String parameter) {
            sender.send(new OnSomethingEvent(parameter));
        }
    }

    private class EventToDummyListener implements MessageSender<Event<DummyListener>> {
        private final DummyListener listener;

        public EventToDummyListener(DummyListener listener) {
            this.listener = listener;
        }

        public void send(Event<DummyListener> message) {
            message.fireOn(listener);
        }
    }

    protected static class DummyRunnable implements Runnable {
        public void run() {
        }
    }
}
