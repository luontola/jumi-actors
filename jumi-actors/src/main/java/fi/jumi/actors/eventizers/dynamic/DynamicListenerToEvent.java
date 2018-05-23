// Copyright © 2011-2018, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers.dynamic;

import fi.jumi.actors.Promise;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;

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
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }
        // The declared return type must be assignable from Promise, because this handler will always
        // return Promise to the caller, even if the callee returns some other Future implementation.
        // The checks in fi.jumi.actors.eventizers.Eventizers#validateActorInterface
        // already limit the declared return type to Promise, Future or ListenableFuture.
        if (method.getReturnType().isAssignableFrom(Promise.class)) {
            Promise.Deferred<T> deferred = Promise.defer();
            target.send(new DynamicEvent<>(method, args, deferred));
            return deferred.promise();
        } else {
            target.send(new DynamicEvent<>(method, args));
            return null;
        }
    }
}
