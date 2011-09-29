// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import org.hamcrest.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.*;

import static fi.jumi.threadsafetyagent.ThreadUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

        Throwable t = getExceptionFromNewThread("T2", new Runnable() {
            public void run() {
                checker.checkCurrentThread();
            }
        });

        assertThat(t, is(instanceOf(AssertionError.class)));
        assertThat(t.getMessage(), is("non-thread-safe instance called from multiple threads: T1, T2"));

        // from which threads it was called
        Throwable cause2 = t.getCause();
        Throwable cause1 = t.getCause().getCause();
        assertThat(cause2.getMessage(), is("called from thread T2"));
        assertThat(cause1.getMessage(), is("called from thread T1"));

        // from where it was called
        assertThat(t, stackTraceContains(getClass().getName()));
    }

    @Test
    public void fails_at_most_once_per_thread() throws Throwable {
        runInNewThread("T1", new Runnable() {
            public void run() {
                checker.checkCurrentThread();
            }
        });
        try {
            checker.checkCurrentThread();
        } catch (AssertionError e) {
            // ignore first failure
        }

        // should not throw an exception
        checker.checkCurrentThread();
    }

    @Test
    public void fails_for_each_new_thread() throws Throwable {
        runInNewThread("T1", new Runnable() {
            public void run() {
                checker.checkCurrentThread();
            }
        });
        try {
            runInNewThread("T2", new Runnable() {
                public void run() {
                    checker.checkCurrentThread();
                }
            });
        } catch (AssertionError e) {
            // ignore first failure
        }

        thrown.expect(AssertionError.class);
        thrown.expect(stackTraceContains("T1"));
        thrown.expect(stackTraceContains("T2"));
        thrown.expect(stackTraceContains("T3"));

        runInNewThread("T3", new Runnable() {
            public void run() {
                checker.checkCurrentThread();
            }
        });
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
            return stackTraceToString(item).indexOf(expected) >= 0;
        }

        private StringBuffer stackTraceToString(Throwable item) {
            StringWriter stackTrace = new StringWriter();
            item.printStackTrace(new PrintWriter(stackTrace));
            return stackTrace.getBuffer();
        }

        public void describeTo(Description description) {
            description.appendText("stack trace contains ").appendValue(expected);
        }
    }
}
