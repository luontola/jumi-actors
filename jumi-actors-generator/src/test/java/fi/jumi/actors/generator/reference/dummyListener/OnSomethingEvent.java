package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.EventToString;
import fi.jumi.actors.generator.DummyListener;
import java.io.Serializable;

public class OnSomethingEvent implements Event<DummyListener>, Serializable {

    private final java.lang.String foo;
    private final java.lang.String bar;

    public OnSomethingEvent(java.lang.String foo, java.lang.String bar) {
        this.foo = foo;
        this.bar = bar;
    }

    public void fireOn(DummyListener target) {
        target.onSomething(foo, bar);
    }

    public String toString() {
        return EventToString.format("DummyListener", "onSomething", foo, bar);
    }
}
