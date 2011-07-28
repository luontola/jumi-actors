// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.api.drivers;

import java.io.Serializable;

/**
 * Uniquely identifies a single test in the tree of all tests. Immutable.
 */
public abstract class TestId implements Comparable<TestId>, Serializable {

    public static final TestId ROOT = new Root();

    public static TestId of(int... path) {
        TestId node = ROOT;
        for (int index : path) {
            node = new Child(node, index);
        }
        return node;
    }

    private TestId() {
        // prevent everybody except the inner classes from extending this class
    }

    // inquiring

    public abstract boolean isRoot();

    public abstract boolean isFirstChild();

    public boolean isAncestorOf(TestId descendant) {
        if (descendant.isRoot()) {
            return false;
        }
        TestId parent = descendant.getParent();
        return this.equals(parent) || this.isAncestorOf(parent);
    }

    public boolean isDescendantOf(TestId ancestor) {
        return ancestor.isAncestorOf(this);
    }

    // accessing relatives

    public abstract TestId getParent();

    public TestId getFirstChild() {
        return new Child(this, 0);
    }

    public abstract TestId getNextSibling();

    // low-level operations

    public abstract int getIndex();

    private int[] getPath() {
        // TODO: make public? should then write explicit tests for it
        return getPath(0);
    }

    private int[] getPath(int depth) {
        if (isRoot()) {
            return new int[depth];
        }
        int[] path = getParent().getPath(depth + 1);
        path[path.length - 1 - depth] = getIndex();
        return path;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TestId) {
            TestId that = (TestId) obj;
            if (this.isRoot() || that.isRoot()) {
                return this.isRoot() == that.isRoot();
            }
            return this.getIndex() == that.getIndex() && this.getParent().equals(that.getParent());
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

    public int compareTo(TestId that) {
        int[] thisPath = this.getPath();
        int[] thatPath = that.getPath();
        for (int i = 0; i < thisPath.length && i < thatPath.length; i++) {
            Integer thisIndex = thisPath[i];
            Integer thatIndex = thatPath[i];
            int comp = thisIndex.compareTo(thatIndex);
            if (comp != 0) {
                return comp;
            }
        }
        return thisPath.length - thatPath.length;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TestId(");
        int[] path = getPath();
        for (int i = 0; i < path.length; i++) {
            int index = path[i];
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(index);
        }
        sb.append(")");
        return sb.toString();
    }


    private static class Root extends TestId {

        public boolean isRoot() {
            return true;
        }

        public boolean isFirstChild() {
            throw new UnsupportedOperationException("root is not a child");
        }

        public TestId getParent() {
            throw new UnsupportedOperationException("root has no parent");
        }

        public TestId getNextSibling() {
            throw new UnsupportedOperationException("root has no siblings");
        }

        public int getIndex() {
            throw new UnsupportedOperationException("root has no index");
        }
    }

    private static class Child extends TestId {

        private final TestId parent;
        private final int index;

        public Child(TestId parent, int index) {
            if (index < 0) {
                throw new IllegalArgumentException("illegal index: " + index);
            }
            this.parent = parent;
            this.index = index;
        }

        public boolean isRoot() {
            return false;
        }

        public boolean isFirstChild() {
            return index == 0;
        }

        public TestId getParent() {
            return parent;
        }

        public TestId getNextSibling() {
            return new Child(parent, index + 1);
        }

        public int getIndex() {
            return index;
        }
    }
}
