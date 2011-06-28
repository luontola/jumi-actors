// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.actors;

import java.lang.reflect.*;

public class DynamicEvent<T> implements Event<T> {

    private final Method method;
    private final Object[] args;

    public DynamicEvent(Method method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    public void fireOn(T target) {
        try {
            method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
