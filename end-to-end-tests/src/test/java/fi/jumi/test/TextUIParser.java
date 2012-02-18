// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import java.util.*;
import java.util.regex.*;

public class TextUIParser {

    private final String output;

    public TextUIParser(String output) {
        this.output = output;
    }

    public int getPassingCount() {
        return findFirstInt(output, "Pass: (\\d+)", 1);
    }

    public int getFailingCount() {
        return findFirstInt(output, "Fail: (\\d+)", 1);
    }

    public int getTotalCount() {
        return findFirstInt(output, "Total: (\\d+)", 1);
    }

    public List<String> getTestStartAndEndEvents() {
        ArrayList<String> events = new ArrayList<String>();
        Matcher m = Pattern.compile(" > \\s*([+|-] .*)").matcher(output);
        while (m.find()) {
            String testNamePrefixed = m.group(1);
            events.add(shortenTestEndEvents(testNamePrefixed));
        }
        return events;
    }

    private String shortenTestEndEvents(String testNamePrefixed) {
        if (testNamePrefixed.startsWith("+ ")) {
            return testNamePrefixed.substring(2);
        } else {
            return "/";
        }
    }

    private static int findFirstInt(String haystack, String regex, int group) {
        return Integer.parseInt(findFirst(haystack, regex, group));
    }

    private static String findFirst(String haystack, String regex, int group) {
        Matcher m = Pattern.compile(regex).matcher(haystack);
        if (!m.find()) {
            throw new IllegalArgumentException("did not find " + regex + " from " + haystack);
        }
        return m.group(group);
    }

    @Override
    public String toString() {
        return output;
    }
}
