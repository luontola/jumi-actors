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

    // TODO: spike code
    /*
    public void update() {
        while (true) {
            TestRunResult result = failedTestRunsStream.poll();
            if (result == null) {
                return;
            }
            print(result);
        }
        if (failedTestRuns.isClosed()) {
            TestSuiteSummary result = summaryStream.poll();
            if (result == null) {
                return;
            }
            print(result)
        }
    }

    public void updateUntilFinished() {
        while (!failedTestRunsStream.isClosed()) {
            TestRunResult result = failedTestRunsStream.take(); // blocking, raises exception if closed
            print(result);
        }
        TestSuiteSummary result = summaryStream.take();
        print(result)
    }

    public boolean isFinished() {
        return failedTestRunsStream.isClosed() && summaryStream.isClosed();
    }
    */
}
