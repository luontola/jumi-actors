// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClassNameMatcherTest {

    @Test
    public void matching_a_single_class() {
        ClassNameMatcher matcher = new ClassNameMatcher("x.Foo");

        assertTrue("that class", matcher.matches("x.Foo"));
        assertFalse("other class", matcher.matches("x.Bar"));
    }

    @Test
    public void matching_all_classes_in_a_package() {
        ClassNameMatcher matcher = new ClassNameMatcher("x.*");

        assertTrue("that package", matcher.matches("x.Foo"));
        assertFalse("subpackage", matcher.matches("x.y.Foo"));
        assertFalse("other package", matcher.matches("y.Foo"));

        assertFalse("Corner case: package with the same prefix", matcher.matches("xx.Foo"));
    }

    @Test
    public void matching_all_classes_in_subpackages() {
        ClassNameMatcher matcher = new ClassNameMatcher("x.**");

        assertTrue("that package", matcher.matches("x.Foo"));
        assertTrue("subpackage", matcher.matches("x.y.Foo"));
        assertFalse("other package", matcher.matches("y.Foo"));

        assertFalse("Corner case: package with the same prefix", matcher.matches("xx.Foo"));
    }
}
