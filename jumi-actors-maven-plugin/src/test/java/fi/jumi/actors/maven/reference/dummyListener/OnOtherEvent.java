package fi.jumi.actors.maven.reference.dummyListener;

import fi.jumi.actors.Event;
import fi.jumi.actors.maven.DummyListener;
import java.io.Serializable;

public class OnOtherEvent implements Event<DummyListener>, Serializable {

    public void fireOn(DummyListener target) {
        target.onOther();
    }

    public String toString() {
        return "DummyListener.onOther()";
    }
}
