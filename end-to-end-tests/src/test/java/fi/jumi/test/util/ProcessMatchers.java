// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.hamcrest.*;

public class ProcessMatchers {

    public static Matcher<Process> alive() {
        return new TypeSafeMatcher<Process>() {
            @Override
            protected boolean matchesSafely(Process item) {
                return isAlive(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("alive");
            }

            @Override
            protected void describeMismatchSafely(Process item, Description mismatchDescription) {
                mismatchDescription.appendText("was dead");
            }
        };
    }

    public static Matcher<Process> dead() {
        return new TypeSafeMatcher<Process>() {
            @Override
            protected boolean matchesSafely(Process item) {
                return !isAlive(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("dead");
            }

            @Override
            protected void describeMismatchSafely(Process item, Description mismatchDescription) {
                mismatchDescription.appendText("was alive");
            }
        };
    }

    private static boolean isAlive(Process item) {
        try {
            item.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }
}
