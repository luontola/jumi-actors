// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import com.google.common.io.ByteStreams;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizer;
import fi.jumi.actors.generator.ast.*;
import fi.jumi.actors.generator.codegen.GeneratedClass;
import fi.jumi.actors.generator.reference.DummyListenerEventizer;
import fi.jumi.actors.queue.*;
import org.junit.*;
import org.junit.rules.*;

import javax.lang.model.element.TypeElement;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@SuppressWarnings("Convert2Diamond") // this class must be compilable with Java 6 because of the annotation processor
public class EventStubGeneratorTest {

    private static final String TARGET_PACKAGE = "fi.jumi.actors.generator.reference";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private EventStubGenerator generator;

    @Before
    public void setUp() {
        generator = newEventStubGenerator(DummyListener.class);
    }

    @Test
    public void eventizer_advertises_its_actor_interface_type() {
        DummyListenerEventizer eventizer = new DummyListenerEventizer();

        assertEquals(DummyListener.class, eventizer.getType());
    }

    @Test
    public void stubs_forward_events_from_frontend_to_backend() {
        DummyListener target = mock(DummyListener.class);
        DummyListenerEventizer eventizer = new DummyListenerEventizer();
        MessageSender<Event<DummyListener>> backend = eventizer.newBackend(target);
        DummyListener frontend = eventizer.newFrontend(backend);

        frontend.onSomething("foo", "bar");
        frontend.onOther();

        verify(target).onSomething("foo", "bar");
        verify(target).onOther();
        verifyNoMoreInteractions(target);
    }

    @Test
    public void event_classes_are_serializable() {
        MessageQueue<Event<DummyListener>> spy = new MessageQueue<Event<DummyListener>>();
        DummyListenerEventizer eventizer = new DummyListenerEventizer();
        DummyListener frontend = eventizer.newFrontend(spy);

        frontend.onSomething("foo", "bar");
        Event<DummyListener> event = spy.poll();

        assertThat(event, is(instanceOf(Serializable.class)));
    }

    @Test
    public void the_events_have_descriptive_toString_methods() {
        MessageQueue<Event<DummyListener>> spy = new MessageQueue<Event<DummyListener>>();
        DummyListenerEventizer eventizer = new DummyListenerEventizer();
        DummyListener frontend = eventizer.newFrontend(spy);

        frontend.onSomething("foo", "bar");
        frontend.onOther();

        assertThat(spy.poll().toString(), is("DummyListener.onSomething(\"foo\", \"bar\")"));
        assertThat(spy.poll().toString(), is("DummyListener.onOther()"));
    }

    @Test
    public void the_generated_events_and_DynamicEvent_have_the_same_toString_format() {
        MessageQueue<Event<DummyListener>> spy = new MessageQueue<Event<DummyListener>>();
        DummyListener generated = new DummyListenerEventizer().newFrontend(spy);
        DummyListener dynamic = new DynamicEventizer<DummyListener>(DummyListener.class).newFrontend(spy);

        generated.onSomething("foo", "bar");
        dynamic.onSomething("foo", "bar");

        generated.onOther();
        dynamic.onOther();

        assertThat(spy.poll().toString(), is(spy.poll().toString()));
        assertThat(spy.poll().toString(), is(spy.poll().toString()));
    }

    @Test
    public void generates_eventizer_class() {
        assertClassEquals("fi.jumi.actors.generator.reference.DummyListenerEventizer", generator.getEventizer());
    }

    @Test
    public void generates_frontend_class() {
        assertClassEquals("fi.jumi.actors.generator.reference.dummyListener.DummyListenerToEvent", generator.getFrontend());
    }

    @Test
    public void generates_backend_class() {
        assertClassEquals("fi.jumi.actors.generator.reference.dummyListener.EventToDummyListener", generator.getBackend());
    }

    @Test
    public void generates_event_classes() throws IOException {
        List<GeneratedClass> events = generator.getEvents();
        assertClassEquals("fi.jumi.actors.generator.reference.dummyListener.OnOtherEvent", events.get(0));
        assertClassEquals("fi.jumi.actors.generator.reference.dummyListener.OnSomethingEvent", events.get(1));
    }

    @Test
    public void generates_event_classes_for_every_listener_method() {
        generator = newEventStubGenerator(TwoMethodInterface.class);

        List<GeneratedClass> events = generator.getEvents();
        assertThat(events.size(), is(2));
        assertThat(events.get(0).name, endsWith(".OnOneEvent"));
        assertThat(events.get(1).name, endsWith(".OnTwoEvent"));
    }

    @Test
    public void adds_imports_for_all_method_parameter_types() {
        Class<?> clazz = ExternalLibraryReferencingListener.class;
        generator = newEventStubGenerator(clazz);

        GeneratedClass event = generator.getEvents().get(0);
        assertThat(event.source, containsString("import java.util.Random;"));

        GeneratedClass frontend = generator.getFrontend();
        assertThat(frontend.source, containsString("import java.util.Random;"));
    }

    @Test
    public void adds_imports_for_type_parameters_of_method_parameter_types() {
        generator = newEventStubGenerator(GenericParametersListener.class);

        GeneratedClass event = generator.getEvents().get(0);
        assertThat(event.source, containsString("import java.util.List;"));
        assertThat(event.source, containsString("import java.io.File;"));

        GeneratedClass frontend = generator.getFrontend();
        assertThat(frontend.source, containsString("import java.util.List;"));
        assertThat(frontend.source, containsString("import java.io.File;"));
    }

    @Test
    public void raw_types_are_not_used() {
        generator = newEventStubGenerator(GenericParametersListener.class);

        GeneratedClass event = generator.getEvents().get(0);
        assertThat(event.source, containsString("List<File>"));
        assertThat(event.source, not(containsString("List ")));

        GeneratedClass frontend = generator.getFrontend();
        assertThat(frontend.source, containsString("List<File>"));
        assertThat(frontend.source, not(containsString("List ")));
    }

    @Ignore
    @Test
    public void supports_methods_inherited_from_parent_interfaces() {
        generator = newEventStubGenerator(ChildInterface.class);

        GeneratedClass frontend = generator.getFrontend();
        assertThat(frontend.source, containsString("void methodInChild()"));
        assertThat(frontend.source, containsString("void methodInParent()"));
    }

    @Ignore
    @Test
    public void rejects_invalid_actor_interfaces() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("actor interface methods must be void");

        newEventStubGenerator(InvalidActorInterface.class);
    }


    private static void assertClassEquals(String expectedName, GeneratedClass actual) {
        assertEquals("class name", expectedName, actual.name);
        assertEquals("file content", readFile(expectedName.replace(".", "/") + ".java"), actual.source);
    }

    private static String readFile(String resource) {
        InputStream in = EventStubGenerator.class.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalArgumentException("No such resource: " + resource);
        }
        try {
            return new String(ByteStreams.toByteArray(in), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private EventStubGenerator newEventStubGenerator(Class<?> listenerType) {
        EventStubGenerator generator = new EventStubGenerator(ast(listenerType), TARGET_PACKAGE);
        generator.setGenerationDate(new GregorianCalendar(2000, Calendar.DECEMBER, 31).getTime());
        return generator;
    }

    private TypeElement ast(Class<?> clazz) {
        return AstExtractor.getAst(clazz, getJavaSource(clazz));
    }

    private static JavaSourceFromString getJavaSource(Class<?> clazz) {
        Class<?> topLevel = getTopLevelClass(clazz);
        String code = readFile(topLevel.getName().replace('.', '/') + ".java");
        return new JavaSourceFromString(topLevel.getName(), code);
    }

    private static Class<?> getTopLevelClass(Class<?> clazz) {
        while (clazz.getEnclosingClass() != null) {
            clazz = clazz.getEnclosingClass();
        }
        return clazz;
    }


    @SuppressWarnings({"UnusedDeclaration"})
    private interface TwoMethodInterface {

        void onOne();

        void onTwo();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private interface ExternalLibraryReferencingListener {

        void onSomething(Random random);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private interface GenericParametersListener {

        void onSomething(List<File> list);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private interface ParentInterface {
        void methodInParent();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private interface ChildInterface extends ParentInterface {
        void methodInChild();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private interface InvalidActorInterface {
        String nonVoidMethod();
    }
}
