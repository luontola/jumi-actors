package fi.jumi.codegenerator.dummy;

import fi.jumi.codegenerator.*;

public class OnSomethingEvent implements MyEvent<DummyListener> {

    private final String param1;
    private final String param2;

    public OnSomethingEvent(String param1, String param2) {
        this.param1 = param1;
        this.param2 = param2;
    }

    public void fireOn(DummyListener target) {
        target.onSomething(param1, param2);
    }
}
