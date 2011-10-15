// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;

public class DefaultTestNotifier implements TestNotifier {

    private final TestId id;
    private final TestClassListener listener;

    public DefaultTestNotifier(TestId id, TestClassListener listener) {
        this.id = id;
        this.listener = listener;
    }

    public void fireFailure(Throwable cause) {
        listener.onFailure(id, cause);
    }

    public void fireTestFinished() {
        listener.onTestFinished(id);
    }
}
