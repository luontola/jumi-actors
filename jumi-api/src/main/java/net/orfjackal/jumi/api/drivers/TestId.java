// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.api.drivers;

import java.util.*;

/**
 * Uniquely identifies a single test in the tree of all tests. Immutable.
 */
public abstract class TestId {

    public static final TestId ROOT = new Root();

    public static TestId of(int... path) {
        TestId node = ROOT;
        for (int index : path) {
            node = new Child(node, index);
        }
        return node;
    }

    private TestId() {
    }

    public abstract boolean isRoot();

    public abstract boolean isFirstChild();

    public abstract TestId getParent();

    public abstract int getIndex();

    public abstract TestId getFirstChild();

    public abstract TestId nextSibling();

    private List<Integer> getPath() {
        List<Integer> path = new ArrayList<Integer>();
        for (TestId node = this; !node.isRoot(); node = node.getParent()) {
            path.add(node.getIndex());
        }
        Collections.reverse(path);
        return Collections.unmodifiableList(path);
    }

    public boolean equals(Object obj) {
        if (obj instanceof TestId) {
            TestId n1 = this;
            TestId n2 = (TestId) obj;
            while (!n1.isRoot() && !n2.isRoot()) {
                if (n1.getIndex() != n2.getIndex()) {
                    return false;
                }
                n1 = n1.getParent();
                n2 = n2.getParent();
            }
            return n1.isRoot() == n2.isRoot();
        }
        return false;
    }

    public int hashCode() {
        // We use an algorithm similar to java.util.Arrays.hashCode()
        int hash = 1;
        for (TestId node = this; !node.isRoot(); node = node.getParent()) {
            hash = 31 * hash + node.getIndex();
        }
        return hash;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TestId(");
        List<Integer> path = getPath();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(path.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    private static class Root extends TestId {

        public boolean isRoot() {
            return true;
        }

        public boolean isFirstChild() {
            throw new AssertionError("not implemented");
        }

        public TestId getParent() {
            throw new AssertionError("not implemented");
        }

        public int getIndex() {
            throw new AssertionError("not implemented");
        }

        public TestId getFirstChild() {
            throw new AssertionError("not implemented");
        }

        public TestId nextSibling() {
            throw new AssertionError("not implemented");
        }
    }


    private static class Child extends TestId {

        private final TestId parent;
        private final int index;

        public Child(TestId parent, int index) {
            this.parent = parent;
            this.index = index;
        }

        public boolean isRoot() {
            return false;
        }

        public boolean isFirstChild() {
            throw new AssertionError("not implemented");
        }

        public TestId getParent() {
            return parent;
        }

        public int getIndex() {
            return index;
        }

        public TestId getFirstChild() {
            throw new AssertionError("not implemented");
        }

        public TestId nextSibling() {
            throw new AssertionError("not implemented");
        }
    }
}
