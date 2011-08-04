package fi.jumi.codegenerator.dummy;

import fi.jumi.codegenerator.*;

public class OnSomethingEvent implements MyEvent<DummyListener> {

    private final String arg0;
    private final String arg1;

    public OnSomethingEvent(String arg0, String arg1) {
        this.arg0 = arg0;
        this.arg1 = arg1;
    }

    public void fireOn(DummyListener target) {
        target.onSomething(arg0, arg1);
    }
}
