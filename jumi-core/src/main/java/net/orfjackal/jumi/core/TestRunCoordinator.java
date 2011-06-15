// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

public class TestRunCoordinator implements CommandListener {

    // TODO: support for multiple clients?
    private SuiteListener listener = null;

    public void addSuiteListener(SuiteListener listener) {
        this.listener = listener;
    }

    public void runTests() {
        listener.onSuiteStarted();
        listener.onSuiteFinished();
    }
}
