// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.utils;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class AssertsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void passes_when_nothing_expected() {
        Asserts.assertContainsSubStrings("anything", new String[]{});
    }

    @Test
    public void passes_when_equals() {
        Asserts.assertContainsSubStrings("needle", new String[]{"needle"});
    }

    @Test
    public void passes_when_contains_one_substring() {
        Asserts.assertContainsSubStrings("before needle after", new String[]{"needle"});
    }

    @Test
    public void passes_when_contains_many_substrings_next_to_each_other() {
        Asserts.assertContainsSubStrings("before needle1needle2 after", new String[]{"needle1", "needle2"});
    }

    @Test
    public void passes_when_contains_many_sub_strings_with_something_in_between() {
        Asserts.assertContainsSubStrings("before needle1 something needle2 after", new String[]{"needle1", "needle2"});
    }

    @Test
    public void fails_with_given_message_when_doesnt_contain_one_substring() {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("failure message");

        Asserts.assertContainsSubStrings("failure message", "something else", new String[]{"needle"});
    }

    @Test
    public void fails_when_doesnt_contain_one_of_many_substrings() {
        thrown.expect(AssertionError.class);

        Asserts.assertContainsSubStrings("before needle after", new String[]{"needle", "missing"});
    }

    @Test
    public void fails_when_doesnt_contain_repeated_instances_of_a_repeated_substrings() {
        thrown.expect(AssertionError.class);

        Asserts.assertContainsSubStrings("needle", new String[]{"needle", "needle"});
    }

    @Test
    public void negated_version_passes_when_doesnt_contain_substring() {
        Asserts.assertNotContainsSubStrings("something else", new String[]{"needle"});
    }

    @Test
    public void negated_version_fails_with_given_message_when_contains_substring() {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("failure message");

        Asserts.assertNotContainsSubStrings("failure message", "needle", new String[]{"needle"});
    }
}
