// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.notifiers;

import net.orfjackal.jumi.api.drivers.*;

public class DefaultSuiteNotifier implements SuiteNotifier {
    public void fireTestFound(TestId id, String name) {
    }

    public TestNotifier fireTestStarted(TestId id) {
        return null;
    }
}
