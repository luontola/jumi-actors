// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.test.guineaPig.OnFooEvent;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class JumiActorsGeneratorTest {

    @Test
    public void generates_eventizers_for_project_interfaces() {
        GuineaPigEventizer eventizer = new GuineaPigEventizer();

        assertThat("type", eventizer.getType(), is(equalTo(GuineaPig.class)));
    }

    @Test
    public void generates_eventizer_frontends() {
        GuineaPigEventizer eventizer = new GuineaPigEventizer();
        MessageQueue<Event<GuineaPig>> queue = new MessageQueue<>();

        GuineaPig frontend = eventizer.newFrontend(queue);
        frontend.onFoo("foo");

        OnFooEvent message = (OnFooEvent) queue.poll();
        assertThat("message", message, is(notNullValue()));
        assertThat("message value", message.getFoo(), is("foo"));
    }

    @Test
    public void generates_eventizer_backends() {
        GuineaPigEventizer eventizer = new GuineaPigEventizer();
        GuineaPig target = mock(GuineaPig.class);

        MessageSender<Event<GuineaPig>> backend = eventizer.newBackend(target);
        backend.send(new OnFooEvent("foo"));

        verify(target).onFoo("foo");
    }

    @Ignore // TODO
    @Test
    public void generates_eventizers_for_3rd_party_interfaces() {
    }
}
