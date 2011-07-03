// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.dynamicevents;

import net.orfjackal.jumi.core.actors.Event;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Arrays;

public class DynamicEvent<T> implements Event<T>, Serializable {

    private transient Method method;
    private final Object[] args;

    // TODO: use events generated at compile time, to get rid of these sub-optimal dynamic events, while keeping the code maintainable
    private final String methodName;

    public DynamicEvent(Method method, Object[] args) {
        this.method = method;
        this.args = args;
        this.methodName = method.getName();
    }

    public void fireOn(T target) {
        if (method == null) {
            method = getMethodByName(methodName, target);
        }
        try {
            method.setAccessible(true);
            method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getMethodByName(String name, Object obj) {
        // XXX: there is some issue which causes Netty to not be able to deserialize java.lang.Class instances, so this hack is needed
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new IllegalArgumentException("method not found: " + name);
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + methodName + ", " + method + ", " + Arrays.toString(args) + ")";
    }
}
