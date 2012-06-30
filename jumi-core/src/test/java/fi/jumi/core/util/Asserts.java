// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.ComparisonFailure;

public class Asserts {

    public static void assertContainsSubStrings(String actual, String[] expectedSubStrings) {
        assertContainsSubStrings("", actual, expectedSubStrings);
    }

    public static void assertContainsSubStrings(String message, String actual, String[] expectedSubStrings) {
        if (!Strings.containsSubStrings(actual, expectedSubStrings)) {
            throw new ComparisonFailure(message, Strings.asLines(expectedSubStrings), actual);
        }
    }

    public static void assertNotContainsSubStrings(String actual, String[] expectedSubStrings) {
        assertNotContainsSubStrings("", actual, expectedSubStrings);
    }

    public static void assertNotContainsSubStrings(String message, String actual, String[] expectedSubStrings) {
        if (Strings.containsSubStrings(actual, expectedSubStrings)) {
            throw new ComparisonFailure(message, Strings.asLines(expectedSubStrings), actual);
        }
    }
}
