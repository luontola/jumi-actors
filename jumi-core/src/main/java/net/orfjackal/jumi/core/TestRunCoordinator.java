// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import java.io.File;
import java.util.List;

public class TestRunCoordinator implements CommandListener {

    // TODO: support for multiple clients?
    private SuiteListener listener = null;

    public void addSuiteListener(SuiteListener listener) {
        this.listener = listener;
    }

    public void runTests(List<File> classPath, String testsToIncludePattern) {
        listener.onSuiteStarted();

        // TODO: find tests
        // TODO: run the tests
        // TODO: wait for the suite to finish (async)

        listener.onSuiteFinished();
    }
}
