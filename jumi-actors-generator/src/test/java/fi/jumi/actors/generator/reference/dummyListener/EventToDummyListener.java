package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.generator.DummyListener;
import fi.jumi.actors.queue.MessageSender;
import javax.annotation.Generated;

@Generated(value = "fi.jumi.actors.generator.EventStubGenerator",
        comments = "Based on fi.jumi.actors.generator.DummyListener",
        date = "2000-12-31")
public class EventToDummyListener implements MessageSender<Event<DummyListener>> {

    private final DummyListener target;

    public EventToDummyListener(DummyListener target) {
        this.target = target;
    }

    @Override
    public void send(Event<DummyListener> message) {
        message.fireOn(target);
    }
}
