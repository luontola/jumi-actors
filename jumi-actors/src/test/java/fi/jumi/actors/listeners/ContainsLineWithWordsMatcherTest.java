// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.listeners;

import org.junit.Test;

import static fi.jumi.actors.Matchers.containsLineWithWords;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContainsLineWithWordsMatcherTest {

    @Test
    public void passes_when_input_contains_only_expected_words() {
        assertThat("foo", containsLineWithWords("foo"));
    }

    @Test(expected = AssertionError.class)
    public void fails_when_input_does_not_contain_any_expected_words() {
        assertThat("foo", containsLineWithWords("bar"));
    }

    @Test
    public void passes_when_at_least_one_line_has_the_expected_words() {
        assertThat("before\nfoo\nafter", containsLineWithWords("foo"));
    }

    @Test
    public void passes_when_the_same_line_has_additional_words_around_the_expected_words() {
        assertThat("before foo after", containsLineWithWords("foo"));
    }

    @Test
    public void passes_when_the_same_line_has_additional_words_between_expected_words() {
        assertThat("foo middle bar", containsLineWithWords("foo", "bar"));
    }

    @Test(expected = AssertionError.class)
    public void fails_when_the_expected_words_are_in_wrong_order() {
        assertThat("bar middle foo", containsLineWithWords("foo", "bar"));
    }

    @Test(expected = AssertionError.class)
    public void fails_when_expected_word_is_not_repeated_enough_many_times() {
        assertThat("xx", containsLineWithWords("x", "x", "x"));
    }

    @Test(expected = AssertionError.class)
    public void fails_when_line_has_only_some_of_the_expected_words() {
        assertThat("foo", containsLineWithWords("foo", "bar"));
    }

    @Test(expected = AssertionError.class)
    public void fails_when_expected_words_are_on_different_lines() {
        assertThat("foo\nbar", containsLineWithWords("foo", "bar"));
    }
}
