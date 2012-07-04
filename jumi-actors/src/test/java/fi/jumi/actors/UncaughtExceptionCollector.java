// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.failures.FailureHandler;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

@ThreadSafe
public class UncaughtExceptionCollector implements Thread.UncaughtExceptionHandler, FailureHandler {

    private final List<Throwable> uncaughtExceptions = Collections.synchronizedList(new ArrayList<Throwable>());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        uncaughtExceptions.add(e);
    }

    @Override
    public void uncaughtException(Object actor, Object message, Throwable exception) {
        uncaughtExceptions.add(exception);
    }

    public RuntimeException throwDummyException() {
        throw new DummyException();
    }

    public void failIfNotEmpty() {
        for (Throwable uncaughtException : uncaughtExceptions) {
            if (!isDummyException(uncaughtException)) {
                throw (AssertionError)
                        new AssertionError("there were exceptions in a background thread")
                                .initCause(uncaughtException);
            }
        }
    }

    private static boolean isDummyException(Throwable t) {
        return t instanceof DummyException;
    }
}
