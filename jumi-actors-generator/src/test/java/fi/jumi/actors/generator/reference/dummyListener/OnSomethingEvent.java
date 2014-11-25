package fi.jumi.actors.generator.reference.dummyListener;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.EventToString;
import fi.jumi.actors.generator.DummyListener;
import java.io.Serializable;
import javax.annotation.Generated;

@Generated(value = "fi.jumi.actors.generator.EventStubGenerator",
        comments = "Based on fi.jumi.actors.generator.DummyListener",
        date = "2000-12-31")
public class OnSomethingEvent implements Event<DummyListener>, Serializable {

    private final String foo;
    private final String bar;

    public OnSomethingEvent(String foo, String bar) {
        this.foo = foo;
        this.bar = bar;
    }

    public String getFoo() {
        return foo;
    }

    public String getBar() {
        return bar;
    }

    @Override
    public void fireOn(DummyListener target) {
        target.onSomething(foo, bar);
    }

    @Override
    public String toString() {
        return EventToString.format("DummyListener", "onSomething", foo, bar);
    }
}
