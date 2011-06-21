// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.events;

import net.orfjackal.jumi.api.drivers.TestId;
import net.orfjackal.jumi.core.SuiteListener;

public class TestFoundEvent implements SuiteEvent {
    private final TestId id;
    private final String name;

    public TestFoundEvent(TestId id, String name) {
        this.id = id;
        this.name = name;
    }

    public void fireOn(SuiteListener target) {
        target.onTestFound(id, name);
    }
}
