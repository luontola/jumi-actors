// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.dynamicevents;

import fi.jumi.actors.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class EventToDynamicListener<T> implements MessageSender<Event<T>> {

    private final T target;

    public EventToDynamicListener(T target) {
        this.target = target;
    }

    @Override
    public void send(Event<T> message) {
        message.fireOn(target);
    }
}
