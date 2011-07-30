package fi.jumi.codegenerator.dummy;

import fi.jumi.codegenerator.*;

public class DummyListenerToEvent implements DummyListener {

    private final MyMessageSender<MyEvent<DummyListener>> sender;

    public DummyListenerToEvent(MyMessageSender<MyEvent<DummyListener>> sender) {
        this.sender = sender;
    }

    public void onSomething(String param1, String param2) {
        sender.send(new OnSomethingEvent(param1, param2));
    }
}
