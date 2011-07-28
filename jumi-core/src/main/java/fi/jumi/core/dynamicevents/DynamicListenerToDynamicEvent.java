// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.dynamicevents;

import fi.jumi.core.actors.*;

import java.lang.reflect.*;

public class DynamicListenerToDynamicEvent<T> implements InvocationHandler {

    private final MessageSender<Event<T>> target;

    public DynamicListenerToDynamicEvent(MessageSender<Event<T>> target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        target.send(new DynamicEvent<T>(method, args));
        return null;
    }
}
