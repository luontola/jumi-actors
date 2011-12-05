package fi.jumi.codegenerator.dummy;

import fi.jumi.actors.*;
import fi.jumi.codegenerator.*;
import java.io.*;

public class OnOtherEvent implements Event<DummyListener>, Serializable {

    public void fireOn(DummyListener target) {
        target.onOther();
    }

    public String toString() {
        return "DummyListener.onOther()";
    }
}
