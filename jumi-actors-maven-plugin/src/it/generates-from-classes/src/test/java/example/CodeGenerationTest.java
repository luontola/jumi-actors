// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package example;

import example.generated.*;
import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.Eventizer;
import fi.jumi.actors.queue.MessageSender;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CodeGenerationTest {

    @Test
    public void generates_a_working_eventizer() {
        Runnable target = mock(Runnable.class);
        Eventizer<Runnable> eventizer = new RunnableEventizer();
        MessageSender<Event<Runnable>> backend = eventizer.newBackend(target);
        Runnable frontend = eventizer.newFrontend(backend);

        frontend.run();

        verify(target).run();
    }
}
