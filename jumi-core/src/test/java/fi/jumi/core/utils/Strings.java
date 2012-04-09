// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.utils;

public class Strings {

    public static boolean containsSubStrings(String actual, String[] expectedSubStrings) {
        int pos = 0;
        for (String expected : expectedSubStrings) {
            pos = actual.indexOf(expected, pos);
            if (pos < 0) {
                return false;
            }
            pos += expected.length();
        }
        return true;
    }

    public static String asLines(String[] ss) {
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
