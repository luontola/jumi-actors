package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.generator.DummyListener;
import fi.jumi.actors.queue.MessageSender;

public class DummyListenerToEvent implements DummyListener {

    private final MessageSender<Event<DummyListener>> target;

    public DummyListenerToEvent(MessageSender<Event<DummyListener>> target) {
        this.target = target;
    }

    @Override
    public void onSomething(String foo, String bar) {
        target.send(new OnSomethingEvent(foo, bar));
    }

    @Override
    public void onOther() {
        target.send(new OnOtherEvent());
    }
}
