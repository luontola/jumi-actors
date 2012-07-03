// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.Eventizer;
import fi.jumi.actors.mq.MessageSender;

public abstract class ActorsContractHelpers<T extends Actors> {

    protected T actors;

    public static final long TIMEOUT = 1000;

    private final EventSpy events = new EventSpy();

    public void logEvent(String event) {
        events.log(event);
    }

    public void awaitEvents(int expectedEventCount) {
        processEvents();
        events.await(expectedEventCount, TIMEOUT);
    }

    protected abstract void processEvents();

    public void assertEvents(String... expected) {
        events.assertContains(expected);
    }

    public void expectNoMoreEvents() {
        events.expectNoMoreEvents();
    }

    public void sendSyncEvent(ActorThread actorThread) {
        ActorRef<Runnable> actor = actorThread.bindActor(Runnable.class, new Runnable() {
            @Override
            public void run() {
                logEvent("sync event");
            }
        });
        actor.tell().run();
    }


    // test doubles

    public class SpyDummyListener implements DummyListener {
        public volatile Thread thread;

        @Override
        public void onSomething(String parameter) {
            thread = Thread.currentThread();
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


    public interface DummyListener {
        void onSomething(String parameter);
    }

    public static class DummyListenerEventizer implements Eventizer<DummyListener> {

        @Override
        public Class<DummyListener> getType() {
            return DummyListener.class;
        }

        @Override
        public DummyListener newFrontend(MessageSender<Event<DummyListener>> target) {
            return new DummyListenerToEvent(target);
        }

        @Override
        public MessageSender<Event<DummyListener>> newBackend(DummyListener target) {
            return new EventToDummyListener(target);
        }
    }

    public static class OnSomethingEvent implements Event<DummyListener> {
        private final String parameter;

        public OnSomethingEvent(String parameter) {
            this.parameter = parameter;
        }

        @Override
        public void fireOn(DummyListener target) {
            target.onSomething(parameter);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof OnSomethingEvent)) {
                return false;
            }
            OnSomethingEvent that = (OnSomethingEvent) obj;
            return this.parameter.equals(that.parameter);
        }
    }

    public static class DummyListenerToEvent implements DummyListener {
        private final MessageSender<Event<DummyListener>> sender;

        public DummyListenerToEvent(MessageSender<Event<DummyListener>> sender) {
            this.sender = sender;
        }

        @Override
        public void onSomething(String parameter) {
            sender.send(new OnSomethingEvent(parameter));
        }
    }

    public static class EventToDummyListener implements MessageSender<Event<DummyListener>> {
        private final DummyListener listener;

        public EventToDummyListener(DummyListener listener) {
            this.listener = listener;
        }

        @Override
        public void send(Event<DummyListener> message) {
            message.fireOn(listener);
        }
    }
}
