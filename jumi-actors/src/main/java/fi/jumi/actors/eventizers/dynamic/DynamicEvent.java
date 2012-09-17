// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers.dynamic;

import fi.jumi.actors.eventizers.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.lang.reflect.*;

@ThreadSafe
public class DynamicEvent<T> implements Event<T>, Serializable {

    private transient Method method;
    private final Object[] args;

    public DynamicEvent(Method method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    @Override
    public void fireOn(T target) {
        try {
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

    @Override
    public String toString() {
        return EventToString.format(method.getDeclaringClass().getSimpleName(), method.getName(), nonNull(args));
    }

    private static Object[] nonNull(Object[] args) {
        if (args == null) {
            return new Object[0];
        }
        return args;
    }
}
