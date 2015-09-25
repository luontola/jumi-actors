// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers.dynamic;

import fi.jumi.actors.eventizers.*;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Supports all actor interfaces using reflection.
 */
@Immutable
public class DynamicEventizerProvider implements EventizerProvider {

    private final ConcurrentHashMap<Class<?>, Eventizer<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Eventizer<T> getEventizerForType(Class<T> type) {
        Eventizer<T> eventizer = (Eventizer<T>) cache.get(type);
        if (eventizer == null) {
            eventizer = new DynamicEventizer<>(type);
            cache.put(type, eventizer);
        }
        return eventizer;
    }
}
