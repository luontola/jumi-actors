// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.api.drivers;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TestIdTest {

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
        assertFalse("null", TestId.of(1).equals(null));
        assertEquals("hashCode for same values", TestId.of(1, 2, 3).hashCode(), TestId.of(1, 2, 3).hashCode());
    }

    @Test
    public void root_is_root() {
        assertThat(TestId.ROOT.isRoot(), is(true));
        assertThat(TestId.of().isRoot(), is(true));
    }

    @Test
    public void other_nodes_are_not_roots() {
        assertThat(TestId.of(0).isRoot(), is(false));
        assertThat(TestId.of(1, 2).isRoot(), is(false));
    }

    @Test
    public void there_is_only_one_root_instance() {
        assertThat(TestId.of(), is(sameInstance(TestId.ROOT)));
    }

//    @Test
//    public void first_child() {
//        TestId root = TestId.ROOT;
//        assertThat(root.getFirstChild(), is(TestId.of(0)));
//        assertThat(root.getFirstChild().getFirstChild(), is(TestId.of(0, 0)));
//    }


//    @Test
//    public void get_parent() {
//        assertThat(TestId.of(0).getParent(), is(TestId.ROOT));
//        assertThat(TestId.of(1).getParent(), is(TestId.ROOT));
//        assertThat(TestId.of(1, 2).getParent(), is(TestId.of(1)));
//    }
}
