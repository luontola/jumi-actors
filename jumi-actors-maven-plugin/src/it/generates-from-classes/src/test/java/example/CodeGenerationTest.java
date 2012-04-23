package example;

import example.generated.*;
import fi.jumi.actors.*;
import fi.jumi.actors.mq.MessageSender;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CodeGenerationTest {

    @Test
    public void generates_a_working_listener_factory() {
        Runnable target = mock(Runnable.class);
        Eventizer<Runnable> eventizer = new RunnableEventizer();
        MessageSender<Event<Runnable>> backend = eventizer.newBackend(target);
        Runnable frontend = eventizer.newFrontend(backend);

        frontend.run();

        verify(target).run();
    }
}
