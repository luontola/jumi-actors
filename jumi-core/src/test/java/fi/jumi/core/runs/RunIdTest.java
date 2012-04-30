// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class RunIdTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void is_a_value_object() {
        RunId id1a = new RunId(1);
        RunId id1b = new RunId(1);
        RunId id2 = new RunId(2);

        assertTrue("equals: itself", id1a.equals(id1a));
        assertTrue("equals: same value", id1a.equals(id1b));
        assertFalse("equals: different value", id1a.equals(id2));
        assertFalse("equals: null", id1a.equals(null));
        assertFalse("equals: other class", id1a.equals(new Object()));

        assertTrue("hashCode: same value", id1a.hashCode() == id1b.hashCode());
        assertFalse("hashCode: different value", id1a.hashCode() == id2.hashCode());
    }

    @Test
    public void has_toString() {
        assertThat(new RunId(1).toString(), is("RunId(1)"));
        assertThat(new RunId(2).toString(), is("RunId(2)"));
    }

    @Test
    public void the_id_must_be_1_or_greater() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id must be 1 or greater, but was: 0");
        new RunId(0);
    }

    @Test
    public void numerical_value_can_be_queried() {
        assertThat(new RunId(1).toInt(), is(1));
        assertThat(new RunId(2).toInt(), is(2));
    }
}
