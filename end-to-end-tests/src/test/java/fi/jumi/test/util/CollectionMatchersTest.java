// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CollectionMatchersTest {

    @Test
    public void containsAtMost_passes_if_actual_equals_expected() {
        List<String> actual = Arrays.asList("a", "b");
        List<String> expected = Arrays.asList("a", "b");

        assertTrue(CollectionMatchers.containsAtMost(expected).matches(actual));
    }

    @Test
    public void containsAtMost_fails_if_actual_has_values_additional_to_expected() {
        List<String> actual = Arrays.asList("a", "b", "c");
        List<String> expected = Arrays.asList("a", "b");

        assertFalse(CollectionMatchers.containsAtMost(expected).matches(actual));
    }

    @Test
    public void containsAtMost_passes_if_actual_has_only_some_of_expected_values() {
        List<String> actual = Arrays.asList("a");
        List<String> expected = Arrays.asList("a", "b");

        assertTrue(CollectionMatchers.containsAtMost(expected).matches(actual));
    }
}
