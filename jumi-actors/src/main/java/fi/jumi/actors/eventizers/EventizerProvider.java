// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers;

import fi.jumi.actors.Actors;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;

/**
 * Determines the types of actors that the {@link Actors} container can create.
 *
 * @see DynamicEventizerProvider
 * @see ComposedEventizerProvider
 */
public interface EventizerProvider {

    <T> Eventizer<T> getEventizerForType(Class<T> type);
}
