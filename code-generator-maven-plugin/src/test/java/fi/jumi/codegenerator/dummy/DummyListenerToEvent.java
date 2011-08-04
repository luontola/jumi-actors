package fi.jumi.codegenerator.dummy;

import fi.jumi.codegenerator.*;

public class DummyListenerToEvent implements DummyListener {

    private final MyMessageSender<MyEvent<DummyListener>> sender;

    public DummyListenerToEvent(MyMessageSender<MyEvent<DummyListener>> sender) {
        this.sender = sender;
    }

    public void onSomething(String arg0, String arg1) {
        sender.send(new OnSomethingEvent(arg0, arg1));
    }
}
