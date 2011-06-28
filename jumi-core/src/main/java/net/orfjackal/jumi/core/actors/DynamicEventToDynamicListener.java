// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

public class DynamicEventToDynamicListener<T> implements MessageSender<Event<T>> {

    private final T target;

    public DynamicEventToDynamicListener(T target) {
        this.target = target;
    }

    public void send(Event<T> message) {
        message.fireOn(target);
    }
}
