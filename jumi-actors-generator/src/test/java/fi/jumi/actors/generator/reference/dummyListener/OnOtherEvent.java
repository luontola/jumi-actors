package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.EventToString;
import fi.jumi.actors.generator.DummyListener;
import java.io.Serializable;
import javax.annotation.Generated;

@Generated(value = "fi.jumi.actors.generator.EventStubGenerator",
        comments = "Based on fi.jumi.actors.generator.DummyListener",
        date = "2000-12-31")
public class OnOtherEvent implements Event<DummyListener>, Serializable {

    @Override
    public void fireOn(DummyListener target) {
        target.onOther();
    }

    @Override
    public String toString() {
        return EventToString.format("DummyListener", "onOther");
    }
}
