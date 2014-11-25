package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.generator.DummyListener;
import fi.jumi.actors.queue.MessageSender;

public class EventToDummyListener implements MessageSender<Event<DummyListener>> {

    private final DummyListener target;

    public EventToDummyListener(DummyListener target) {
        this.target = target;
    }

    public void send(Event<DummyListener> message) {
        message.fireOn(target);
    }
}
