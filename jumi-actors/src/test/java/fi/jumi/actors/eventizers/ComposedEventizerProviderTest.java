// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers;

import fi.jumi.actors.Event;
import fi.jumi.actors.queue.MessageSender;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ComposedEventizerProviderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void returns_an_eventizer_which_corresponds_the_specified_type() {
        Class<Integer> type1 = Integer.class;
        Eventizer<Integer> eventizer1 = new DummyEventizer<Integer>(type1);
        Class<Double> type2 = Double.class;
        Eventizer<Double> eventizer2 = new DummyEventizer<Double>(type2);

        ComposedEventizerProvider provider = new ComposedEventizerProvider(eventizer1, eventizer2);

        assertThat(provider.getEventizerForType(type1), is(eventizer1));
        assertThat(provider.getEventizerForType(type2), is(eventizer2));
    }

    @Test
    public void cannot_return_eventizers_for_unsupported_types() {
        ComposedEventizerProvider provider = new ComposedEventizerProvider();

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported type");
        thrown.expectMessage(NoEventizerForThisListener.class.getName());

        provider.getEventizerForType(NoEventizerForThisListener.class);
    }


    private interface NoEventizerForThisListener {
    }

    private static class DummyEventizer<T> implements Eventizer<T> {
        private final Class<T> type;

        private DummyEventizer(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<T> getType() {
            return type;
        }

        @Override
        public T newFrontend(MessageSender<Event<T>> target) {
            return null;
        }

        @Override
        public MessageSender<Event<T>> newBackend(Object target) {
            return null;
        }
    }
}
