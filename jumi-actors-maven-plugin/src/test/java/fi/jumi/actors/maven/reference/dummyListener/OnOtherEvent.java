package fi.jumi.actors.maven.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.EventToString;
import fi.jumi.actors.maven.DummyListener;
import java.io.Serializable;

public class OnOtherEvent implements Event<DummyListener>, Serializable {

    public void fireOn(DummyListener target) {
        target.onOther();
    }

    public String toString() {
        return EventToString.format("DummyListener", "onOther");
    }
}
