// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.dynamicevents;

import fi.jumi.actors.Event;
import fi.jumi.actors.mq.MessageSender;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.*;

@ThreadSafe
public class DynamicListenerToEvent<T> implements InvocationHandler {

    private final MessageSender<Event<T>> target;

    public DynamicListenerToEvent(MessageSender<Event<T>> target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        target.send(new DynamicEvent<T>(method, args));
        return null;
    }
}
