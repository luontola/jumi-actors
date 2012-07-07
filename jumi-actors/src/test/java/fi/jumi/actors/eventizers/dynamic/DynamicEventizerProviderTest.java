// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers.dynamic;

import fi.jumi.actors.eventizers.Eventizer;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class DynamicEventizerProviderTest {

    @Test
    public void returns_an_eventizer_for_any_listener_interface() {
        DynamicEventizerProvider provider = new DynamicEventizerProvider();

        Eventizer<DummyListener> eventizer = provider.getEventizerForType(DummyListener.class);

        assertThat(eventizer.getType()).isEqualTo(DummyListener.class);
    }

    @Test
    public void caches_the_eventizer_instances() {
        DynamicEventizerProvider provider = new DynamicEventizerProvider();

        Eventizer<DummyListener> eventizer1 = provider.getEventizerForType(DummyListener.class);
        Eventizer<DummyListener> eventizer2 = provider.getEventizerForType(DummyListener.class);

        assertThat(eventizer1).isSameAs(eventizer2);
    }


    private interface DummyListener {
    }
}
