package example;

import example.generated.*;
import fi.jumi.actors.*;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CodeGenerationTest {

    @Test
    public void foo() {
        ExampleListener target = mock(ExampleListener.class);
        ListenerFactory<ExampleListener> factory = new ExampleListenerFactory();
        MessageSender<Event<ExampleListener>> backend = factory.newBackend(target);
        ExampleListener frontend = factory.newFrontend(backend);

        frontend.onSomething();

        verify(target).onSomething();
    }
}
