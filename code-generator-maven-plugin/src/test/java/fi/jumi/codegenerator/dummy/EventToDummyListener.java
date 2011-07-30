package fi.jumi.codegenerator.dummy;

import fi.jumi.codegenerator.*;

public class EventToDummyListener implements MyMessageSender<MyEvent<DummyListener>> {

    private final DummyListener listener;

    public EventToDummyListener(DummyListener listener) {
        this.listener = listener;
    }

    public void send(MyEvent<DummyListener> message) {
        message.fireOn(listener);
    }
}
