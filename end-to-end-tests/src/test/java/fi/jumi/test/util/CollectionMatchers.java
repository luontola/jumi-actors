// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.hamcrest.*;

import java.util.*;

public class CollectionMatchers {

    public static <T> Matcher<Collection<T>> containsAtMost(final Collection<T> expected) {
        return new TypeSafeMatcher<Collection<T>>() {
            @Override
            protected boolean matchesSafely(Collection<T> actual) {
                return getUnexpected(actual).isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains at most ")
                        .appendValueList("", ", ", "", expected);
            }

            @Override
            protected void describeMismatchSafely(Collection<T> actual, Description mismatchDescription) {
                mismatchDescription.appendText("contained unexpected values: ")
                        .appendValueList("", ", ", "", getUnexpected(actual));
            }

            private Collection<T> getUnexpected(Collection<T> actual) {
                Collection<T> unexpected = new ArrayList<>(actual);
                unexpected.removeAll(expected);
                return unexpected;
            }
        };
    }
}
