package fi.jumi.actors.maven.reference;

import fi.jumi.actors.Event;
import fi.jumi.actors.eventizers.Eventizer;
import fi.jumi.actors.maven.DummyListener;
import fi.jumi.actors.maven.reference.dummyListener.*;
import fi.jumi.actors.mq.MessageSender;

public class DummyListenerEventizer implements Eventizer<DummyListener> {

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
