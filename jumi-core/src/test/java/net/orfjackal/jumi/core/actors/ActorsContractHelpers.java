// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class ActorsContractHelpers<T extends Actors> {

    protected T actors;

    public static final long TIMEOUT = 1000;

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

    public class SpyDummyListener implements DummyListener {
        public volatile Thread thread;

        public void onSomething(String parameter) {
            thread = Thread.currentThread();
            logEvent(parameter);
        }
    }

    public class SpyRunnable implements Runnable {
        private final String event;
        public volatile Thread thread;

        public SpyRunnable(String event) {
            this.event = event;
        }

        public void run() {
            thread = Thread.currentThread();
            logEvent(event);
        }
    }

    public class WorkerStartingSpyRunnable extends SpyRunnable {
        private final Runnable worker;
        private final Runnable onFinished;

        public WorkerStartingSpyRunnable(String event, Runnable worker, Runnable onFinished) {
            super(event);
            this.worker = worker;
            this.onFinished = onFinished;
        }

        @Override
        public void run() {
            super.run();

            // starting the worker must be done inside an actor
            actors.startUnattendedWorker(worker, onFinished);
        }
    }

    public class ExceptionThrowingSpyRunnable extends SpyRunnable {
        private final RuntimeException exception;

        public ExceptionThrowingSpyRunnable(String event, RuntimeException exception) {
            super(event);
            this.exception = exception;
        }

        @Override
        public void run() {
            super.run();

            // the stack trace will be funny, because it doesn't start from where it was thrown; let's wrap it
            throw new RuntimeException(exception);
        }
    }

    public static class NullRunnable implements Runnable {
        public void run() {
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
}
