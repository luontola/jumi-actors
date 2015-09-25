// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers;

import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Future;

@Immutable
public class Eventizers {

    private Eventizers() {
        // utility class, not to be instantiated
    }

    public static void validateActorInterface(Class<?> type) {
        checkIsInterface(type);
        for (Method method : type.getMethods()) {
            checkReturnTypeIsAllowed(type, method);
            checkDoesNotThrowExceptions(type, method);
        }
    }

    private static void checkIsInterface(Class<?> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("actor interfaces must be interfaces, but got " + type);
        }
    }

    private static void checkReturnTypeIsAllowed(Class<?> type, Method method) {
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(Void.TYPE) && !Future.class.isAssignableFrom(returnType)) {
            throw new IllegalArgumentException("actor interface methods must return void or " + Future.class.getName() + ", " +
                    "but method " + method.getName() + " of " + type + " had return type " + returnType.getName());
        }
    }

    private static void checkDoesNotThrowExceptions(Class<?> type, Method method) {
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length > 0) {
            throw new IllegalArgumentException("actor interface methods may not throw exceptions, " +
                    "but method " + method.getName() + " of " + type + " throws " + format(exceptionTypes));
        }
    }

    private static String format(Class<?>[] types) {
        List<String> names = new ArrayList<String>();
        for (Class<?> type : types) {
            names.add(type.getName());
        }
        String s = names.toString();
        return s.substring(1, s.length() - 1);
    }
}
