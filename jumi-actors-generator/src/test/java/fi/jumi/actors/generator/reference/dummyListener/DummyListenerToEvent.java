package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.generator.DummyListener;
import fi.jumi.actors.queue.MessageSender;

public class DummyListenerToEvent implements DummyListener {

    private final MessageSender<Event<DummyListener>> sender;

    public DummyListenerToEvent(MessageSender<Event<DummyListener>> sender) {
        this.sender = sender;
    }

    public void onOther() {
        sender.send(new OnOtherEvent());
    }

    public void onSomething(String arg0, String arg1) {
        sender.send(new OnSomethingEvent(arg0, arg1));
    }
}
