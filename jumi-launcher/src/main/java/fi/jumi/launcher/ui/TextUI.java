// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.core.SuiteResults;

import java.io.PrintStream;

public class TextUI {

    private final PrintStream out;
    private final PrintStream err;
    private final SuiteResults results;

    public TextUI(PrintStream out, PrintStream err, SuiteResults results) {
        this.out = out;
        this.err = err;
        this.results = results;
    }

    public void runToCompletion() {
        int totalTests = results.getTotalTests();
        int passingTests = results.getPassingTests();
        int failingTests = results.getFailingTests();

        for (Throwable t : results.getFailureExceptions()) {
            t.printStackTrace(err);
        }

        out.println("Pass: " + passingTests + ", Fail: " + failingTests + ", Total: " + totalTests);
    }
}
