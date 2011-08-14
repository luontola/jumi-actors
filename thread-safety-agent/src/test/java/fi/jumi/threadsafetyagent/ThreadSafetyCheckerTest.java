// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import org.hamcrest.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.util.concurrent.*;

public class ThreadSafetyCheckerTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final ThreadSafetyChecker checker = new ThreadSafetyChecker();

    @Test
    public void is_silent_when_called_from_just_one_thread() throws Throwable {
        runInNewThread("T1", new Runnable() {
            public void run() {
                checker.checkCurrentThread();
                checker.checkCurrentThread();
            }
        });
    }

    @Test
    public void throws_an_exception_when_called_from_many_threads() throws Throwable {
        runInNewThread("T1", new Runnable() {
            public void run() {
                checker.checkCurrentThread();
            }
        });

        thrown.expect(AssertionError.class);
        thrown.expectMessage("non-thread-safe instance called from multiple threads");

        // from which threads it was called
        thrown.expect(stackTraceContains("T1"));
        thrown.expect(stackTraceContains("T2"));

        // from where it was called
        thrown.expect(stackTraceContains(getClass().getName()));

        runInNewThread("T2", new Runnable() {
            public void run() {
                checker.checkCurrentThread();
            }
        });
    }

    private static void runInNewThread(String threadName, Runnable target) throws Throwable {
        FutureTask<Object> future = new FutureTask<Object>(target, null);
        new Thread(future, threadName).start();
        try {
            future.get();
        } catch (ExecutionException e) {
            //e.getCause().printStackTrace();
            throw e.getCause();
        }
    }

    private StackTraceContainsMatcher stackTraceContains(String s) {
        return new StackTraceContainsMatcher(s);
    }

    private static class StackTraceContainsMatcher extends TypeSafeMatcher<Throwable> {
        private final String expected;

        public StackTraceContainsMatcher(String expected) {
            this.expected = expected;
        }

        protected boolean matchesSafely(Throwable item) {
            StringWriter stackTrace = new StringWriter();
            item.printStackTrace(new PrintWriter(stackTrace));
            return stackTrace.getBuffer().indexOf(expected) >= 0;
        }

        public void describeTo(Description description) {
            description.appendText("stack trace contains ").appendValue(expected);
        }
    }
}
