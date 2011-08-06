// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.dynamicevents;

import fi.jumi.core.actors.Event;

import java.io.*;
import java.lang.reflect.*;
import java.util.Arrays;

public class DynamicEvent<T> implements Event<T>, Serializable {

    private transient Method method;
    private final Object[] args;

    public DynamicEvent(Method method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    public void fireOn(T target) {
        try {
            method.setAccessible(true);
            method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(method.getName());
        out.writeObject(method.getDeclaringClass());
        out.writeObject(method.getParameterTypes());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String name = (String) in.readObject();
        Class<?> declaringClass = (Class<?>) in.readObject();
        Class<?>[] parameterTypes = (Class<?>[]) in.readObject();
        try {
            method = declaringClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + method + ", " + Arrays.toString(args) + ")";
    }
}
