// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.actors.*;
import fi.jumi.core.*;
import fi.jumi.core.events.suite.SuiteListenerToEvent;
import fi.jumi.launcher.ui.TextUI;
import org.junit.Test;

import java.io.*;
import java.util.regex.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TextUIParserTest {

    private final MessageQueue<Event<SuiteListener>> stream = new MessageQueue<Event<SuiteListener>>();
    private final SuiteListener listener = new SuiteListenerToEvent(stream);

    @Test
    public void get_number_of_passing_tests() {
        SuiteMother.emptySuite(listener);
        assertThat(parsePassingCount(textUI()), is(0));

        SuiteMother.onePassingTest(listener);
        assertThat(parsePassingCount(textUI()), is(1));
    }

    @Test
    public void get_number_of_failing_tests() {
        SuiteMother.emptySuite(listener);
        assertThat(parseFailingCount(textUI()), is(0));

        SuiteMother.oneFailingTest(listener);
        assertThat(parseFailingCount(textUI()), is(1));
    }

    @Test
    public void get_total_number_of_tests() {
        SuiteMother.emptySuite(listener);
        assertThat(parseTotalCount(textUI()), is(0));

        SuiteMother.onePassingTest(listener);
        assertThat(parseTotalCount(textUI()), is(1));

        SuiteMother.oneFailingTest(listener);
        assertThat(parseTotalCount(textUI()), is(1));
    }

    private int parseTotalCount(String output) {
        return findFirstInt(output, "Total: (\\d+)", 1);
    }

    private int parsePassingCount(String output) {
        return findFirstInt(output, "Pass: (\\d+)", 1);
    }

    private int parseFailingCount(String output) {
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

    private String textUI() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TextUI ui = new TextUI(new PrintStream(out), new PrintStream(out), stream);
        ui.update();
        return out.toString();
    }
}
