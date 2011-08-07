// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.dynamicevents;

import fi.jumi.actors.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class DynamicEventListenerTest {

    private final DynamicListenerFactory<DummyListener> factory = new DynamicListenerFactory<DummyListener>(DummyListener.class);
    private final MessageQueue<Event<DummyListener>> queue = new MessageQueue<Event<DummyListener>>();
    private final DummyListener frontend = factory.newFrontend(queue);

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void method_calls_are_converted_into_event_objects() {
        frontend.onSomething("param");

        assertThat(queue.poll(), is(notNullValue()));
    }

    @Test
    public void event_objects_are_converted_back_into_method_calls() {
        DummyListener target = mock(DummyListener.class);
        MessageSender<Event<DummyListener>> backend = factory.newBackend(target);

        frontend.onSomething("param");
        Event<DummyListener> event = queue.poll();
        backend.send(event);

        verify(target).onSomething("param");
    }

    @Test
    public void listeners_may_contain_only_void_methods() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("listeners may contain only void methods, but onSomething had return type java.lang.String");

        new DynamicListenerFactory<ListenerWithNonVoidMethods>(ListenerWithNonVoidMethods.class);
    }


    // test data

    private interface DummyListener {
        void onSomething(String parameter);
    }

    private interface ListenerWithNonVoidMethods {
        @SuppressWarnings({"UnusedDeclaration"})
        String onSomething(String parameter);
    }
}
