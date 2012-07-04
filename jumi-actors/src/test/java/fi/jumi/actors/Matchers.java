// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import org.hamcrest.*;

public class Matchers {

    public static Matcher<String> containsLineWithWords(final String... expectedWords) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                for (String line : item.split("\n")) {
                    if (lineContainsExpectedWords(line)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean lineContainsExpectedWords(String line) {
                int pos = 0;
                for (String expectedWord : expectedWords) {
                    pos = line.indexOf(expectedWord, pos);
                    if (pos < 0) {
                        return false;
                    }
                    pos++;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains line with words ").appendValueList("", ", ", "", expectedWords);
            }
        };
    }

    public static Matcher<Throwable> hasCause(final Matcher<?> causeMatcher) {
        return new TypeSafeMatcher<Throwable>() {
            @Override
            protected boolean matchesSafely(Throwable item) {
                return causeMatcher.matches(item.getCause());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has cause ")
                        .appendDescriptionOf(causeMatcher);
            }
        };
    }
}
