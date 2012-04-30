// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ComposedEventizerLocator implements EventizerLocator {

    private final Eventizer<?>[] eventizers;

    public ComposedEventizerLocator(Eventizer<?>... eventizers) {
        this.eventizers = eventizers;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T> Eventizer<T> getEventizerForType(Class<T> type) {
        for (Eventizer<?> eventizer : eventizers) {
            if (eventizer.getType().equals(type)) {
                return (Eventizer<T>) eventizer;
            }
        }
        throw new IllegalArgumentException("unsupported type: " + type);
    }
}
