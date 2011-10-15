// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DefaultSuiteNotifier implements SuiteNotifier {

    private final TestClassListener listener;

    public DefaultSuiteNotifier(TestClassListener listener) {
        this.listener = listener;
    }

    public void fireTestFound(TestId id, String name) {
        listener.onTestFound(id, name);
    }

    public TestNotifier fireTestStarted(TestId id) {
        listener.onTestStarted(id);
        return new DefaultTestNotifier(id, listener);
    }
}
