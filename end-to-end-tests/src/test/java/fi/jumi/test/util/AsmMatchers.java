// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.hamcrest.*;
import org.objectweb.asm.tree.ClassNode;

public class AsmMatchers {

    public static Matcher<ClassNode> anInterface() {
        return new ClassNodeMatcher() {

            @Override
            protected boolean matchesSafely(ClassNode item) {
                return AsmUtils.isInterface(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an interface");
            }
        };
    }

    public static Matcher<ClassNode> syntheticClass() {
        return new ClassNodeMatcher() {
            @Override
            protected boolean matchesSafely(ClassNode item) {
                return AsmUtils.isSynthetic(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("synthetic class");
            }
        };
    }

    public static Matcher<ClassNode> nameStartsWithOneOf(final String[] prefixes) {
        return new ClassNodeMatcher() {

            @Override
            protected boolean matchesSafely(ClassNode item) {
                for (String prefix : prefixes) {
                    if (item.name.startsWith(prefix)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("name starts with one of ")
                        .appendValueList("", ", ", "", prefixes);
            }
        };
    }

    public static Matcher<ClassNode> hasClassVersion(final Matcher<Integer> versionMatcher) {
        return new ClassNodeMatcher() {

            @Override
            protected boolean matchesSafely(ClassNode item) {
                return versionMatcher.matches(item.version);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has class version ")
                        .appendDescriptionOf(versionMatcher);
            }

            @Override
            protected void describeMismatchSafely(ClassNode item, Description mismatchDescription) {
                mismatchDescription.appendText("was version ")
                        .appendValue(item.version);
            }
        };
    }

    public static CompositeMatcher<ClassNode> newClassNodeCompositeMatcher() {
        return new ClassNodeCompositeMatcher();
    }

    private static class ClassNodeCompositeMatcher extends CompositeMatcher<ClassNode> {

        @Override
        protected String describeItem(ClassNode item) {
            return item.name;
        }
    }

    private static abstract class ClassNodeMatcher extends TypeSafeMatcher<ClassNode> {
        @Override
        protected void describeMismatchSafely(ClassNode item, Description mismatchDescription) {
            mismatchDescription.appendText("was <class ").appendText(item.name).appendText(">");
        }
    }
}
