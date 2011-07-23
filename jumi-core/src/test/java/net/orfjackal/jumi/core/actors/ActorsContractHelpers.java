// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class ActorsContractHelpers {

    private static final long TIMEOUT = 1000;

    private final Queue<String> events = new ConcurrentLinkedQueue<String>();

    public void logEvent(String event) {
        events.add(event);
    }

    public void awaitEvents(int expectedEventCount) {
        processEvents();

        long limit = System.currentTimeMillis() + TIMEOUT;
        while (events.size() < expectedEventCount) {
            if (System.currentTimeMillis() > limit) {
                throw new AssertionError("timed out; received events " + events + " but expected " + expectedEventCount);
            }
            Thread.yield();
        }
    }

    protected abstract void processEvents();

    public void assertEvents(String... expected) {
        List<String> actual = new ArrayList<String>(events);
        assertThat("events", actual, is(Arrays.asList(expected)));
    }


    // test doubles

    public class EventLoggingActor implements DummyListener {
        public void onSomething(String parameter) {
            logEvent(parameter);
        }
    }

    public class CurrentThreadSpy implements DummyListener {
        public volatile Thread actorThread;

        public void onSomething(String parameter) {
            actorThread = Thread.currentThread();
            logEvent(parameter);
        }
    }


    // guinea pigs

    public interface PrimaryInterface {
        void onPrimaryEvent();
    }

    public interface SecondaryInterface {
        void onSecondaryEvent();
    }

    public interface NoFactoryForThisListener {
    }

    public interface DummyListener {
        void onSomething(String parameter);
    }

    public class DummyListenerFactory implements ListenerFactory<DummyListener> {

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

    public class OnSomethingEvent implements Event<DummyListener> {
        private final String parameter;

        public OnSomethingEvent(String parameter) {
            this.parameter = parameter;
        }

        public void fireOn(DummyListener target) {
            target.onSomething(parameter);
        }
    }

    public class DummyListenerToEvent implements DummyListener {
        private final MessageSender<Event<DummyListener>> sender;

        public DummyListenerToEvent(MessageSender<Event<DummyListener>> sender) {
            this.sender = sender;
        }

        public void onSomething(String parameter) {
            sender.send(new OnSomethingEvent(parameter));
        }
    }

    public class EventToDummyListener implements MessageSender<Event<DummyListener>> {
        private final DummyListener listener;

        public EventToDummyListener(DummyListener listener) {
            this.listener = listener;
        }

        public void send(Event<DummyListener> message) {
            message.fireOn(listener);
        }
    }

    public static class DummyRunnable implements Runnable {
        public void run() {
        }
    }
}
