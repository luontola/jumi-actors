// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import java.util.concurrent.*;

public class ThreadUtil {

    public static void runInNewThread(String threadName, Runnable target) throws Throwable {
        FutureTask<Object> future = new FutureTask<>(target, null);
        new Thread(future, threadName).start();
        try {
            future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    public static Throwable getExceptionFromNewThread(String threadName, Runnable target) {
        try {
            runInNewThread(threadName, target);
        } catch (Throwable throwable) {
            return throwable;
        }
        throw new AssertionError("Expected to throw an exception but did not throw anything");
    }
}
