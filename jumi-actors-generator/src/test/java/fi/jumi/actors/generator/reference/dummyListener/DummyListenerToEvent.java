package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.generator.DummyListener;

public class DummyListenerToEvent implements DummyListener {

    private final fi.jumi.actors.queue.MessageSender<fi.jumi.actors.eventizers.Event<fi.jumi.actors.generator.DummyListener>> sender;

    public DummyListenerToEvent(fi.jumi.actors.queue.MessageSender<fi.jumi.actors.eventizers.Event<fi.jumi.actors.generator.DummyListener>> sender) {
        this.sender = sender;
    }

    public void onSomething(java.lang.String foo, java.lang.String bar) {
        sender.send(new OnSomethingEvent(foo, bar));
    }

    public void onOther() {
        sender.send(new OnOtherEvent());
    }
}
