// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.api.drivers;

/**
 * Uniquely identifies a single test in the tree of all tests. Immutable.
 */
public class TestId {

    public static final TestId ROOT = new TestId();

    public static TestId to(int... indices) {
        return null;
    }

    public TestId getFirstChild() {
        return null;
    }

    public TestId nextSibling() {
        return null;
    }

    public TestId getParent() {
        return null;
    }

    // TODO: some more helper methods such as isRoot(), isFirstChild() etc.
}
