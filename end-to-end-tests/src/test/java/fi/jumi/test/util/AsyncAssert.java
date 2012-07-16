// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.hamcrest.Matcher;

import static org.hamcrest.MatcherAssert.assertThat;

public class AsyncAssert {

    public static <T> void assertEventually(T actual, Matcher<? super T> matcher, long timeout) {
        assertEventually("", actual, matcher, timeout);
    }

    public static <T> void assertEventually(String reason, T actual, Matcher<? super T> matcher, long timeout) {
        long limit = System.currentTimeMillis() + timeout;
        AssertionError error;
        do {
            error = checkAssert(reason, actual, matcher);
            if (error == null) {
                return;
            }
        } while (System.currentTimeMillis() < limit);
        throw error;
    }

    private static <T> AssertionError checkAssert(String reason, T actual, Matcher<? super T> matcher) {
        try {
            assertThat(reason, actual, matcher);
            return null;
        } catch (AssertionError e) {
            return e;
        }
    }
}
