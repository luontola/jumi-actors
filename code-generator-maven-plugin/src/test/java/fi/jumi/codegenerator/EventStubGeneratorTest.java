// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import fi.jumi.codegenerator.dummy.DummyListenerFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class EventStubGeneratorTest {

    @Test
    public void factory_advertises_its_listener_type() {
        DummyListenerFactory factory = new DummyListenerFactory();

        assertEquals(DummyListener.class, factory.getType());
    }

    @Test
    public void stubs_forward_events_from_frontend_to_backend() {
        DummyListener target = mock(DummyListener.class);
        DummyListenerFactory factory = new DummyListenerFactory();
        MyMessageSender<MyEvent<DummyListener>> backend = factory.newBackend(target);
        DummyListener frontend = factory.newFrontend(backend);

        frontend.onSomething("foo", "bar");

        verify(target).onSomething("foo", "bar");
    }
}
