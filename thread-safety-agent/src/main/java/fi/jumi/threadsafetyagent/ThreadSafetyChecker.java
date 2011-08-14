// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import java.util.Collection;
import java.util.concurrent.*;

public class ThreadSafetyChecker {

    private final ConcurrentMap<Thread, CallLocation> calledFromThreads = new ConcurrentHashMap<Thread, CallLocation>();

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public void checkCurrentThread() {
        Thread currentThread = Thread.currentThread();
        if (!calledFromThreads.containsKey(currentThread)) {
            calledFromThreads.put(currentThread, new CallLocation());
        }
        if (calledFromThreads.size() > 1) {
            AssertionError e = new AssertionError("non-thread-safe instance called from multiple threads");
            e.initCause(chainUp(calledFromThreads.values()));
            throw e;
        }
    }

    private static Throwable chainUp(Collection<? extends Throwable> exceptions) {
        Throwable cause = null;
        for (Throwable e : exceptions) {
            cause = e.initCause(cause);
        }
        return cause;
    }

    private static class CallLocation extends RuntimeException {
        public CallLocation() {
            super("Called from thread " + Thread.currentThread());
        }
    }
}
