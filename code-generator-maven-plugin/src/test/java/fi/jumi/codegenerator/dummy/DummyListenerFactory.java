package fi.jumi.codegenerator.dummy;

import fi.jumi.codegenerator.*;

public class DummyListenerFactory implements MyListenerFactory<DummyListener> {

    public Class<DummyListener> getType() {
        return DummyListener.class;
    }

    public DummyListener newFrontend(MyMessageSender<MyEvent<DummyListener>> target) {
        return new DummyListenerToEvent(target);
    }

    public MyMessageSender<MyEvent<DummyListener>> newBackend(DummyListener target) {
        return new EventToDummyListener(target);
    }
}
