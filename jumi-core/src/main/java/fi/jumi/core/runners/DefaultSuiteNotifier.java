// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;

public class DefaultSuiteNotifier implements SuiteNotifier {

    private final TestClassRunner listener;

    public DefaultSuiteNotifier(TestClassRunner listener) {
        this.listener = listener;
    }

    public void fireTestFound(TestId id, String name) {
        listener.onTestFound(id, name);
    }

    public TestNotifier fireTestStarted(TestId id) {
        return null; // TODO
    }
}
