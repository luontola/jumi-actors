// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import java.lang.reflect.*;

public class DynamicEventListener {

    private DynamicEventListener() {
    }

    public static <T> T newFrontend(Class<T> type, MessageSender<Event<T>> queue) {
        for (Method method : type.getMethods()) {
            checkReturnTypeIsVoid(method);
        }
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                new DynamicListenerToDynamicEvent<T>(queue))
        );
    }

    private static void checkReturnTypeIsVoid(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE)) {
            throw new IllegalArgumentException("listeners may contain only void methods, but " +
                    method.getName() + " had return type " + returnType.getName());
        }
    }

    public static <T> MessageSender<Event<T>> newBackend(T target) {
        return new DynamicEventToDynamicListener<T>(target);
    }
}
