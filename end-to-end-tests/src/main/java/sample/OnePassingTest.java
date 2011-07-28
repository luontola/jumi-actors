// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import fi.jumi.api.RunVia;

@RunVia(SimpleUnit.class)
@SuppressWarnings({"UnusedDeclaration"})
public class OnePassingTest {

    public void testPassing() {
    }

    public void unrelatedMethod() {
        // doesn't start with "test", so is not a test
    }
}
