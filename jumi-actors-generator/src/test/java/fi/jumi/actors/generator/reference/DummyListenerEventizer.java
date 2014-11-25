package fi.jumi.actors.generator.reference;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.Eventizer;
import fi.jumi.actors.generator.DummyListener;
import fi.jumi.actors.generator.reference.dummyListener.*;
import fi.jumi.actors.queue.MessageSender;

public class DummyListenerEventizer implements Eventizer<DummyListener> {

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
