// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import fi.jumi.api.RunVia;
import fi.jumi.test.simpleunit.SimpleUnit;

@RunVia(SimpleUnit.class)
@SuppressWarnings({"UnusedDeclaration"})
public class OneFailingTest {

    public void testFailing() {
        throw new AssertionError("dummy failure");
    }
}
