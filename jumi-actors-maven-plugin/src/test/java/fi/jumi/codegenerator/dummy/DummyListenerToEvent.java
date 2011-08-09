package fi.jumi.codegenerator.dummy;

import fi.jumi.actors.*;
import fi.jumi.codegenerator.*;

public class DummyListenerToEvent implements DummyListener {

    private final MessageSender<Event<DummyListener>> sender;

    public DummyListenerToEvent(MessageSender<Event<DummyListener>> sender) {
        this.sender = sender;
    }

    public void onSomething(String arg0, String arg1) {
        sender.send(new OnSomethingEvent(arg0, arg1));
    }
}
