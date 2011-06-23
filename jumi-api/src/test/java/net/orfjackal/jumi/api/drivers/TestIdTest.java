// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.api.drivers;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TestIdTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    // low-level operations

    @Test
    public void to_string() {
        assertThat(TestId.of().toString(), is("TestId()"));
        assertThat(TestId.of(0).toString(), is("TestId(0)"));
        assertThat(TestId.of(1).toString(), is("TestId(1)"));
        assertThat(TestId.of(1, 2).toString(), is("TestId(1, 2)"));
        assertThat(TestId.of(1, 2, 3).toString(), is("TestId(1, 2, 3)"));
    }

    @Test
    public void is_a_value_object() {
        assertTrue("same value", TestId.of(1).equals(TestId.of(1)));
        assertFalse("different value", TestId.of(1).equals(TestId.of(2)));
        assertFalse("many path elements", TestId.of(1, 3, 1).equals(TestId.of(1, 2, 1)));
        assertFalse("longer & shorter", TestId.of(1, 2).equals(TestId.of(1)));
        assertFalse("shorter & longer", TestId.of(1).equals(TestId.of(1, 2)));
        assertFalse("root & child", TestId.ROOT.equals(TestId.of(1)));
        assertFalse("child & root", TestId.of(1).equals(TestId.ROOT));
        assertFalse("null", TestId.of(1).equals(null));
        assertEquals("hashCode for same values", TestId.of(1, 2, 3).hashCode(), TestId.of(1, 2, 3).hashCode());
    }

    @Test
    public void hashCode_has_good_dispersion() {
        List<TestId> values = new ArrayList<TestId>();
        values.add(TestId.of());
        values.add(TestId.of(0));
        values.add(TestId.of(1));
        values.add(TestId.of(2));
        values.add(TestId.of(3));
        values.add(TestId.of(0, 0));
        values.add(TestId.of(1, 0));
        values.add(TestId.of(0, 1));
        values.add(TestId.of(2, 0));
        values.add(TestId.of(0, 2));
        values.add(TestId.of(1, 1));
        values.add(TestId.of(1, 2));
        values.add(TestId.of(2, 1));

        Set<Integer> hashCodes = new HashSet<Integer>();
        for (TestId value : values) {
            hashCodes.add(value.hashCode());
        }
        assertThat(hashCodes.size(), is(values.size()));
    }

    @Test
    public void ordering() {
        List<TestId> expectedOrder = Arrays.asList(
                TestId.of(),
                TestId.of(0),
                TestId.of(0, 0),
                TestId.of(0, 1),
                TestId.of(0, 2),
                TestId.of(1),
                TestId.of(1, 0),
                TestId.of(1, 1),
                TestId.of(1, 2),
                TestId.of(2)
        );

        List<TestId> actualOrder = new ArrayList<TestId>(expectedOrder);
        Collections.shuffle(actualOrder);
        Collections.sort(actualOrder);

        assertThat("natural order", actualOrder, is(expectedOrder));
        for (TestId id : expectedOrder) {
            assertThat("comparing with self: " + id, id.compareTo(id), is(0));
        }
    }

    @Test
    public void get_index() {
        assertThat(TestId.of(0).getIndex(), is(0));
        assertThat(TestId.of(1).getIndex(), is(1));
        assertThat(TestId.of(1, 2).getIndex(), is(2));
    }

    @Test
    public void root_has_no_index() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("root has no index");
        TestId.ROOT.getIndex();
    }

    @Test
    public void negative_indices_are_not_allowed() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("illegal index: -1");
        TestId.of(-1);
    }

    @Test
    public void overflows_are_prevented() {
        TestId lastSibling = TestId.of(Integer.MAX_VALUE);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("illegal index: -2147483648");
        lastSibling.getNextSibling();
    }

    // inquiring

    @Test
    public void is_root() {
        assertThat(TestId.ROOT.isRoot(), is(true));
        assertThat(TestId.of().isRoot(), is(true));
        assertThat(TestId.of(0).isRoot(), is(false));
        assertThat(TestId.of(1, 2).isRoot(), is(false));
    }

    @Test
    public void there_is_only_one_root_instance() {
        assertThat(TestId.of(), is(sameInstance(TestId.ROOT)));
    }

    @Test
    public void is_first_child() {
        assertThat(TestId.of(0).isFirstChild(), is(true));
        assertThat(TestId.of(1).isFirstChild(), is(false));
        assertThat(TestId.of(1, 2, 0).isFirstChild(), is(true));
        assertThat(TestId.of(1, 2, 3).isFirstChild(), is(false));
    }

    @Test
    public void root_is_not_a_child() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("root is not a child");
        TestId.ROOT.isFirstChild();
    }

    @Test
    public void is_ancestor_of() {
        TestId x = TestId.of(1, 2, 3);

        assertThat("grand child of x", TestId.of(1, 2, 3, 4, 5).isAncestorOf(x), is(false));
        assertThat("child of x", TestId.of(1, 2, 3, 4).isAncestorOf(x), is(false));
        assertThat("itself", TestId.of(1, 2, 3).isAncestorOf(x), is(false));
        assertThat("parent of x", TestId.of(1, 2).isAncestorOf(x), is(true));
        assertThat("grand parent of x", TestId.of(1).isAncestorOf(x), is(true));

        assertThat("root", TestId.of().isAncestorOf(x), is(true));
        assertThat("root reverse", x.isAncestorOf(TestId.of()), is(false));

        assertThat("sibling of x", TestId.of(1, 2, 10).isAncestorOf(x), is(false));
        assertThat("cousin of x", TestId.of(1, 10, 3).isAncestorOf(x), is(false));
        assertThat("second cousin of x", TestId.of(10, 2, 3).isAncestorOf(x), is(false));
    }

    @Test
    public void is_descendant_of() {
        TestId x = TestId.of(1, 2, 3);

        assertThat("grand child of x", TestId.of(1, 2, 3, 4, 5).isDescendantOf(x), is(true));
        assertThat("child of x", TestId.of(1, 2, 3, 4).isDescendantOf(x), is(true));
        assertThat("itself", TestId.of(1, 2, 3).isDescendantOf(x), is(false));
        assertThat("parent of x", TestId.of(1, 2).isDescendantOf(x), is(false));
        assertThat("grand parent of x", TestId.of(1).isDescendantOf(x), is(false));

        assertThat("root", TestId.of().isDescendantOf(x), is(false));
        assertThat("root reverse", x.isDescendantOf(TestId.of()), is(true));

        assertThat("sibling of x", TestId.of(1, 2, 10).isDescendantOf(x), is(false));
        assertThat("cousin of x", TestId.of(1, 10, 3).isDescendantOf(x), is(false));
        assertThat("second cousin of x", TestId.of(10, 2, 3).isDescendantOf(x), is(false));
    }

    // accessing relatives

    @Test
    public void get_parent() {
        assertThat(TestId.of(0).getParent(), is(TestId.ROOT));
        assertThat(TestId.of(1).getParent(), is(TestId.ROOT));
        assertThat(TestId.of(1, 2).getParent(), is(TestId.of(1)));
    }

    @Test
    public void root_has_no_parent() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("root has no parent");
        TestId.ROOT.getParent();
    }

    @Test
    public void get_first_child() {
        assertThat(TestId.ROOT.getFirstChild(), is(TestId.of(0)));
        assertThat(TestId.ROOT.getFirstChild().getFirstChild(), is(TestId.of(0, 0)));
        assertThat(TestId.of(1, 2, 3).getFirstChild(), is(TestId.of(1, 2, 3, 0)));
    }

    @Test
    public void get_next_sibling() {
        assertThat(TestId.of(0).getNextSibling(), is(TestId.of(1)));
        assertThat(TestId.of(1).getNextSibling(), is(TestId.of(2)));
        assertThat(TestId.of(1, 2, 3).getNextSibling(), is(TestId.of(1, 2, 4)));
    }

    @Test
    public void root_has_no_siblings() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("root has no siblings");
        TestId.ROOT.getNextSibling();
    }
}
