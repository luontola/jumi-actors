// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@ThreadSafe
public class ThreadSafetyChecker {

    private final Set<Thread> calledFromThreads = new CopyOnWriteArraySet<Thread>();
    private CallLocation callLocations = null;

    public void checkCurrentThread() {
        Thread currentThread = Thread.currentThread();
        // TODO: performance optimization; check last thread against a non-volatile field?
        if (!calledFromThreads.contains(currentThread)) {
            registerCall(currentThread);
        }
    }

    private synchronized void registerCall(Thread currentThread) {
        calledFromThreads.add(currentThread);
        callLocations = new CallLocation(callLocations);
        if (calledFromThreads.size() > 1) {
            AssertionError e = new AssertionError("non-thread-safe instance called from multiple threads: " + threadNames(calledFromThreads)) {
                public Throwable fillInStackTrace() {
                    return this;
                }
            };
            e.initCause(callLocations);
            throw e;
        }
    }

    private static String threadNames(Set<Thread> threads) {
        List<String> threadNames = new ArrayList<String>();
        for (Thread thread : threads) {
            threadNames.add(thread.getName());
        }
        Collections.sort(threadNames);
        String s = "";
        for (String threadName : threadNames) {
            if (s.length() > 0) {
                s += ", ";
            }
            s += threadName;
        }
        return s;
    }

    private static class CallLocation extends RuntimeException {
        public CallLocation(CallLocation previousLocations) {
            super("called from thread: " + Thread.currentThread().getName(), previousLocations);
        }
    }
}
