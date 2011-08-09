package example;

import example.generated.*;
import fi.jumi.actors.*;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CodeGenerationTest {

    @Test
    public void generates_a_working_listener_factory() {
        Runnable target = mock(Runnable.class);
        ListenerFactory<Runnable> factory = new RunnableFactory();
        MessageSender<Event<Runnable>> backend = factory.newBackend(target);
        Runnable frontend = factory.newFrontend(backend);

        frontend.run();

        verify(target).run();
    }
}
