// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import org.junit.Test;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ActorsTest {
    private static final long TIMEOUT = 1000;

    private Actors actors = new Actors(new DummyListenerFactory());

    @Test
    public void method_calls_on_handle_are_forwarded_to_target() throws InterruptedException {
        final SynchronousQueue<String> eventParameter = new SynchronousQueue<String>();
        DummyListener target = new DummyListener() {
            public void onSomething(String parameter) {
                try {
                    eventParameter.put(parameter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        DummyListener handle = actors.createNewActor(DummyListener.class, target, "ActorName");
        handle.onSomething("event parameter");

        String parameter = eventParameter.poll(TIMEOUT, TimeUnit.MILLISECONDS);
        assertThat(parameter, is("event parameter"));
    }

    @Test
    public void target_is_invoked_in_its_own_actor_thread() throws InterruptedException {
        final SynchronousQueue<Thread> actorThread = new SynchronousQueue<Thread>();
        DummyListener target = new DummyListener() {
            public void onSomething(String parameter) {
                try {
                    actorThread.put(Thread.currentThread());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        DummyListener handle = actors.createNewActor(DummyListener.class, target, "ActorName");
        handle.onSomething(null);

        Thread thread = actorThread.poll(TIMEOUT, TimeUnit.MILLISECONDS);
        assertThat(thread.getName(), is("ActorName"));
    }

    // TODO: bind actors to current thread
}


interface DummyListener {
    void onSomething(String parameter);
}

class DummyListenerFactory implements ListenerFactory<DummyListener> {

    public Class<DummyListener> getType() {
        return DummyListener.class;
    }

    public DummyListener createSenderWrapper(MessageSender<Event<DummyListener>> sender) {
        return new DummyEventSender(sender);
    }

    public MessageSender<Event<DummyListener>> createReceiver(DummyListener listener) {
        return new DummyEventReceiver(listener);
    }
}

class SomethingEvent implements Event<DummyListener> {
    private final String parameter;

    public SomethingEvent(String parameter) {
        this.parameter = parameter;
    }

    public void fireOn(DummyListener target) {
        target.onSomething(parameter);
    }
}

class DummyEventSender implements DummyListener {
    private final MessageSender<Event<DummyListener>> sender;

    public DummyEventSender(MessageSender<Event<DummyListener>> sender) {
        this.sender = sender;
    }

    public void onSomething(String parameter) {
        sender.send(new SomethingEvent(parameter));
    }
}

class DummyEventReceiver implements MessageSender<Event<DummyListener>> {
    private final DummyListener listener;

    public DummyEventReceiver(DummyListener listener) {
        this.listener = listener;
    }

    public void send(Event<DummyListener> message) {
        message.fireOn(listener);
    }
}
