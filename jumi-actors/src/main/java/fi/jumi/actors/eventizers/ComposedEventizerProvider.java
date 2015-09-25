// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers;

import javax.annotation.concurrent.Immutable;
import java.util.*;

/**
 * To be used with generated or hand-written {@link Eventizer}s.
 */
@Immutable
public class ComposedEventizerProvider implements EventizerProvider {

    private final Map<Class<?>, Eventizer<?>> eventizers;

    public ComposedEventizerProvider(Eventizer<?>... eventizers) {
        HashMap<Class<?>, Eventizer<?>> map = new HashMap<>();
        for (Eventizer<?> eventizer : eventizers) {
            map.put(eventizer.getType(), eventizer);
        }
        this.eventizers = Collections.unmodifiableMap(map);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T> Eventizer<T> getEventizerForType(Class<T> type) {
        Eventizer<?> eventizer = eventizers.get(type);
        if (eventizer == null) {
            throw new IllegalArgumentException("unsupported type: " + type);
        }
        return (Eventizer<T>) eventizer;
    }
}
