// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.utils;

import org.junit.ComparisonFailure;

public class Asserts {

    public static void assertContainsSubStrings(String actual, String[] expectedStrings) {
        assertContainsSubStrings("", actual, expectedStrings);
    }

    public static void assertContainsSubStrings(String message, String actual, String[] expectedStrings) {
        int pos = 0;
        for (String expected : expectedStrings) {
            pos = actual.indexOf(expected, pos);
            if (pos < 0) {
                throw new ComparisonFailure(message, asLines(expectedStrings), actual);
            }
        }
    }

    public static void assertNotContainsSubStrings(String actual, String[] expectedStrings) {
        assertNotContainsSubStrings("", actual, expectedStrings);
    }

    public static void assertNotContainsSubStrings(String message, String actual, String[] expectedStrings) {
        int pos = 0;
        for (String expected : expectedStrings) {
            pos = actual.indexOf(expected, pos);
            if (pos < 0) {
                return; // not found, assertion passes
            }
        }
        throw new ComparisonFailure(message, asLines(expectedStrings), actual);
    }

    private static String asLines(String[] ss) {
        StringBuilder sb = new StringBuilder();
        for (String s : ss) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
