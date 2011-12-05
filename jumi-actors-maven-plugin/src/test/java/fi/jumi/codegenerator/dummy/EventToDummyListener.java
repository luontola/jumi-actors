package fi.jumi.codegenerator.dummy;

import fi.jumi.actors.Event;
import fi.jumi.actors.MessageSender;
import fi.jumi.codegenerator.DummyListener;

public class EventToDummyListener implements MessageSender<Event<DummyListener>> {

    private final DummyListener listener;

    public EventToDummyListener(DummyListener listener) {
        this.listener = listener;
    }

    public void send(Event<DummyListener> message) {
        message.fireOn(listener);
    }
}
