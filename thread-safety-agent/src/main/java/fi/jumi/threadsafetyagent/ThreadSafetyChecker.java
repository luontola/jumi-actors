// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

@ThreadSafe
public class ThreadSafetyChecker {

    /**
     * Not volatile, because it is not required for thread safety;
     * it would only would impose a read barrier and might reduce performance.
     */
    private Thread lastThread = null;

    private final Set<Thread> calledFromThreads = new HashSet<Thread>(1, 1);
    private CallLocation callLocations = null;


    public void checkCurrentThread() {
        Thread currentThread = Thread.currentThread();

        // Performance optimization: avoid checking a thread twice
        // when called repeatedly from the same thread, which is the most common case.
        if (currentThread == lastThread) {
            return;
        }
        lastThread = currentThread;

        fullCheck(currentThread);
    }

    private synchronized void fullCheck(Thread currentThread) {
        if (calledFromThreads.contains(currentThread)) {
            return;
        }
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
