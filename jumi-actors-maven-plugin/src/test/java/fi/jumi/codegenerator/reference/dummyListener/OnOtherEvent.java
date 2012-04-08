package fi.jumi.codegenerator.reference.dummyListener;

import fi.jumi.actors.Event;
import fi.jumi.codegenerator.DummyListener;
import java.io.Serializable;

public class OnOtherEvent implements Event<DummyListener>, Serializable {

    public void fireOn(DummyListener target) {
        target.onOther();
    }

    public String toString() {
        return "DummyListener.onOther()";
    }
}
