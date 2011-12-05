package fi.jumi.codegenerator.dummy;

import fi.jumi.actors.*;
import fi.jumi.codegenerator.*;
import java.io.*;

public class OnSomethingEvent implements Event<DummyListener>, Serializable {

    private final String arg0;
    private final String arg1;

    public OnSomethingEvent(String arg0, String arg1) {
        this.arg0 = arg0;
        this.arg1 = arg1;
    }

    public void fireOn(DummyListener target) {
        target.onSomething(arg0, arg1);
    }

    public String toString() {
        return "DummyListener.onSomething(" + arg0 + ", " + arg1 + ")";
    }
}
