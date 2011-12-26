// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiLauncher;

import java.io.PrintStream;

public class TextUI {

    // XXX: spike code

    private final PrintStream out;
    private final PrintStream err;
    private final JumiLauncher launcher;

    public TextUI(PrintStream out, PrintStream err, JumiLauncher launcher) {
        this.out = out;
        this.err = err;
        this.launcher = launcher;
    }

    public void run() throws InterruptedException {
        launcher.awaitSuiteFinished();
        int totalTests = launcher.getTotalTests();
        int passingTests = launcher.getPassingTests();
        int failingTests = launcher.getFailingTests();

        out.println("Pass: " + passingTests + ", Fail: " + failingTests + ", Total: " + totalTests);
    }
}
