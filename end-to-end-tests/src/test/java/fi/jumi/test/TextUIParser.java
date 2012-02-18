// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import java.util.regex.*;

public class TextUIParser {

    public final String output;

    public TextUIParser(String output) {
        this.output = output;
    }

    public int getTotalCount() {
        return findFirstInt(output, "Total: (\\d+)", 1);
    }

    public int getPassingCount() {
        return findFirstInt(output, "Pass: (\\d+)", 1);
    }

    public int getFailingCount() {
        return findFirstInt(output, "Fail: (\\d+)", 1);
    }

    private static int findFirstInt(String haystack, String regex, int group) {
        return Integer.parseInt(findFirst(haystack, regex, group));
    }

    private static String findFirst(String haystack, String regex, int group) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(haystack);
        if (!m.find()) {
            throw new IllegalArgumentException("did not find " + regex + " from " + haystack);
        }
        return m.group(group);
    }
}
