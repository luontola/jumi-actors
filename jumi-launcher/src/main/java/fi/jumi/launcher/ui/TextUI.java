// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.Event;
import fi.jumi.actors.queue.MessageReceiver;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.results.*;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.PrintStream;

@NotThreadSafe
public class TextUI {

    private final PrintStream out;
    private final PrintStream err;

    // TODO: if multiple readers are needed, create a Streamer class per the original designs
    private final MessageReceiver<Event<SuiteListener>> eventStream;

    private final SuiteEventDemuxer demuxer = new SuiteEventDemuxer();
    private final SuitePrinter suitePrinter = new SuitePrinter();

    public TextUI(PrintStream out, PrintStream err, MessageReceiver<Event<SuiteListener>> eventStream) {
        this.out = out;
        this.err = err;
        this.eventStream = eventStream;
    }

    public void update() {
        while (!demuxer.isSuiteFinished()) {
            Event<SuiteListener> message = eventStream.poll();
            if (message == null) {
                break;
            }
            updateWithMessage(message);
        }
    }

    public void updateUntilFinished() throws InterruptedException {
        while (!demuxer.isSuiteFinished()) {
            Event<SuiteListener> message = eventStream.take();
            updateWithMessage(message);
        }
    }

    private void updateWithMessage(Event<SuiteListener> message) {
        demuxer.send(message);
        message.fireOn(suitePrinter);
    }

    @NotThreadSafe
    private class SuitePrinter implements SuiteListener {

        @Override
        public void onSuiteStarted() {
        }

        @Override
        public void onTestFound(String testClass, TestId testId, String name) {
        }

        @Override
        public void onRunStarted(RunId runId, String testClass) {
        }

        @Override
        public void onTestStarted(RunId runId, TestId testId) {
        }

        @Override
        public void onFailure(RunId runId, Throwable cause) {
        }

        @Override
        public void onTestFinished(RunId runId) {
        }

        @Override
        public void onRunFinished(RunId runId) {
            // TODO: option for printing only failing or all runs
            demuxer.visitRun(runId, new RunPrinter());
        }

        @Override
        public void onSuiteFinished() {
            SuiteResultsSummary summary = new SuiteResultsSummary();
            demuxer.visitAllRuns(summary);
            printSuiteFooter(summary);
        }

        // visual style

        private void printSuiteFooter(SuiteResultsSummary summary) {
            int pass = summary.getPassingTests();
            int fail = summary.getFailingTests();
            int total = summary.getTotalTests();
            out.println(String.format("Pass: %d, Fail: %d, Total: %d", pass, fail, total));
        }
    }

    @NotThreadSafe
    private class RunPrinter implements RunVisitor {

        private int testNestingLevel = 0;

        @Override
        public void onRunStarted(RunId runId, String testClass) {
            printRunHeader(testClass, runId);
        }

        @Override
        public void onTestStarted(RunId runId, String testClass, TestId testId) {
            testNestingLevel++;
            printTestName("+", testClass, testId);
        }

        @Override
        public void onFailure(RunId runId, String testClass, TestId testId, Throwable cause) {
            cause.printStackTrace(err);
        }

        @Override
        public void onTestFinished(RunId runId, String testClass, TestId testId) {
            printTestName("-", testClass, testId);
            testNestingLevel--;
        }

        @Override
        public void onRunFinished(RunId runId, String testClass) {
            printRunFooter();
        }

        // visual style

        private void printRunHeader(String testClass, RunId runId) {
            out.println(" > Run #" + runId.toInt() + " in " + testClass);
        }

        private void printTestName(String bullet, String testClass, TestId testId) {
            out.println(" > " + testNameIndent() + bullet + " " + demuxer.getTestName(testClass, testId));
        }

        private void printRunFooter() {
            out.println();
        }

        private String testNameIndent() {
            StringBuilder indent = new StringBuilder();
            for (int i = 1; i < testNestingLevel; i++) {
                indent.append("  ");
            }
            return indent.toString();
        }
    }
}
