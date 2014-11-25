package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.EventToString;
import fi.jumi.actors.generator.DummyListener;
import java.io.Serializable;

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
