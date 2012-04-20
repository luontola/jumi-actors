// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.dynamicevents;

import fi.jumi.actors.*;

import javax.annotation.concurrent.Immutable;
import java.lang.reflect.*;

@Immutable
public class DynamicListenerFactory<T> implements ListenerFactory<T> {

    private final Class<T> type;

    @SuppressWarnings({"unchecked"})
    public static ListenerFactory<?>[] factoriesFor(Class<?>... types) {
        ListenerFactory<?>[] factories = new ListenerFactory<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            factories[i] = new DynamicListenerFactory(types[i]);
        }
        return factories;
    }

    public DynamicListenerFactory(Class<T> type) {
        for (Method method : type.getMethods()) {
            checkReturnTypeIsVoid(method);
        }
        this.type = type;
    }

    private static void checkReturnTypeIsVoid(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE)) {
            throw new IllegalArgumentException("listeners may contain only void methods, but " +
                    method.getName() + " had return type " + returnType.getName());
        }
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T newFrontend(MessageSender<Event<T>> target) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                new DynamicListenerToEvent<T>(target))
        );
    }

    @Override
    public MessageSender<Event<T>> newBackend(T target) {
        return new EventToDynamicListener<T>(target);
    }
}
