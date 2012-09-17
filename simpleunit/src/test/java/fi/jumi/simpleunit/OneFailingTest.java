// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.simpleunit;

import fi.jumi.api.RunVia;

@RunVia(SimpleUnit.class)
@SuppressWarnings({"UnusedDeclaration"})
public class OneFailingTest {

    public void testFailing() {
        throw new AssertionError("dummy failure");
    }
}
