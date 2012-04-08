// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import fi.jumi.actors.*;
import fi.jumi.codegenerator.dummy.DummyListenerFactory;
import fi.jumi.codegenerator.java.GeneratedClass;
import org.apache.commons.io.IOUtils;
import org.junit.*;

import java.io.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class EventStubGeneratorTest {

    private static final String TARGET_PACKAGE = "fi.jumi.codegenerator.dummy";

    private EventStubGenerator generator;

    @Before
    public void setUp() {
        generator = new EventStubGenerator(DummyListener.class, TARGET_PACKAGE);
    }

    @Test
    public void factory_advertises_its_listener_type() {
        DummyListenerFactory factory = new DummyListenerFactory();

        assertEquals(DummyListener.class, factory.getType());
    }

    @Test
    public void stubs_forward_events_from_frontend_to_backend() {
        DummyListener target = mock(DummyListener.class);
        DummyListenerFactory factory = new DummyListenerFactory();
        MessageSender<Event<DummyListener>> backend = factory.newBackend(target);
        DummyListener frontend = factory.newFrontend(backend);

        frontend.onSomething("foo", "bar");
        frontend.onOther();

        verify(target).onSomething("foo", "bar");
        verify(target).onOther();
        verifyNoMoreInteractions(target);
    }

    @Test
    public void event_classes_are_serializable() {
        MessageQueue<Event<DummyListener>> spy = new MessageQueue<Event<DummyListener>>();
        DummyListenerFactory factory = new DummyListenerFactory();
        DummyListener frontend = factory.newFrontend(spy);

        frontend.onSomething("foo", "bar");
        Event<DummyListener> event = spy.poll();

        assertThat(event, is(instanceOf(Serializable.class)));
    }

    @Test
    public void the_events_have_descriptive_toString_methods() {
        MessageQueue<Event<DummyListener>> spy = new MessageQueue<Event<DummyListener>>();
        DummyListenerFactory factory = new DummyListenerFactory();
        DummyListener frontend = factory.newFrontend(spy);

        frontend.onSomething("foo", "bar");
        frontend.onOther();

        assertThat(spy.poll().toString(), is("DummyListener.onSomething(foo, bar)"));
        assertThat(spy.poll().toString(), is("DummyListener.onOther()"));
    }

    @Test
    public void generates_factory_class() throws IOException {
        assertClassEquals("fi/jumi/codegenerator/dummy/DummyListenerFactory.java", generator.getFactory());
    }

    @Test
    public void generates_frontend_class() throws IOException {
        assertClassEquals("fi/jumi/codegenerator/dummy/DummyListenerToEvent.java", generator.getFrontend());
    }

    @Test
    public void generates_backend_class() throws IOException {
        assertClassEquals("fi/jumi/codegenerator/dummy/EventToDummyListener.java", generator.getBackend());
    }

    @Test
    public void generates_event_classes() throws IOException {
        List<GeneratedClass> events = generator.getEvents();
        assertClassEquals("fi/jumi/codegenerator/dummy/OnOtherEvent.java", events.get(0));
        assertClassEquals("fi/jumi/codegenerator/dummy/OnSomethingEvent.java", events.get(1));
    }

    @Test
    public void generates_event_classes_for_every_listener_method() {
        generator = new EventStubGenerator(TwoMethodInterface.class, TARGET_PACKAGE);

        List<GeneratedClass> events = generator.getEvents();
        assertThat(events.size(), is(2));
        assertThat(events.get(0).path, endsWith("OnOneEvent.java"));
        assertThat(events.get(1).path, endsWith("OnTwoEvent.java"));
    }

    @Test
    public void adds_imports_for_all_method_parameter_types() {
        generator = new EventStubGenerator(ExternalLibraryReferencingListener.class, TARGET_PACKAGE);

        GeneratedClass event = generator.getEvents().get(0);
        assertThat(event.source, containsString("import java.util.Random;"));

        GeneratedClass frontend = generator.getFrontend();
        assertThat(frontend.source, containsString("import java.util.Random;"));
    }

    @Test
    public void adds_imports_for_type_parameters_of_method_parameter_types() {
        generator = new EventStubGenerator(GenericParametersListener.class, TARGET_PACKAGE);

        GeneratedClass event = generator.getEvents().get(0);
        assertThat(event.source, containsString("import java.util.List;"));
        assertThat(event.source, containsString("import java.io.File;"));

        GeneratedClass frontend = generator.getFrontend();
        assertThat(frontend.source, containsString("import java.util.List;"));
        assertThat(frontend.source, containsString("import java.io.File;"));
    }

    @Test
    public void raw_types_are_not_used() {
        generator = new EventStubGenerator(GenericParametersListener.class, TARGET_PACKAGE);

        GeneratedClass event = generator.getEvents().get(0);
        assertThat(event.source, containsString("List<File>"));
        assertThat(event.source, not(containsString("List ")));

        GeneratedClass frontend = generator.getFrontend();
        assertThat(frontend.source, containsString("List<File>"));
        assertThat(frontend.source, not(containsString("List ")));
    }

    @Test
    public void supports_methods_inherited_from_parent_interfaces() {
        generator = new EventStubGenerator(ChildInterface.class, TARGET_PACKAGE);

        GeneratedClass frontend = generator.getFrontend();
        assertThat(frontend.source, containsString("void methodInChild()"));
        assertThat(frontend.source, containsString("void methodInParent()"));
    }


    private static void assertClassEquals(String expectedPath, GeneratedClass actual) throws IOException {
        assertEquals(expectedPath, actual.path);
        assertEquals(readFile(expectedPath), actual.source);
    }

    private static String readFile(String resource) throws IOException {
        return IOUtils.toString(EventStubGenerator.class.getClassLoader().getResourceAsStream(resource));
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
}
