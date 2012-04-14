// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.*;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.PrintStream;
import java.util.*;

@NotThreadSafe
public class TextUI {

    private final PrintStream out;
    private final PrintStream err;

    // TODO: if multiple readers are needed, create a Streamer class per the original designs
    private final MessageReceiver<Event<SuiteListener>> eventStream;
    private boolean suiteFinished = false;

    private final Map<GlobalTestId, String> testNames = new HashMap<GlobalTestId, String>();
    private final Map<RunId, RunState> runs = new HashMap<RunId, RunState>();

    // TODO: extract counting to its own class
    private final Set<GlobalTestId> failedTests = new HashSet<GlobalTestId>();
    private final Set<GlobalTestId> allTests = new HashSet<GlobalTestId>();

    private SuitePrinter suitePrinter = new SuitePrinter();
    private Event<SuiteListener> currentMessage;

    public TextUI(PrintStream out, PrintStream err, MessageReceiver<Event<SuiteListener>> eventStream) {
        this.out = out;
        this.err = err;
        this.eventStream = eventStream;
    }

    public void update() {
        while (!suiteFinished) {
            Event<SuiteListener> message = eventStream.poll();
            if (message == null) {
                break;
            }
            updateWithMessage(message);
        }
    }

    public void updateUntilFinished() throws InterruptedException {
        while (!suiteFinished) {
            Event<SuiteListener> message = eventStream.take();
            updateWithMessage(message);
        }
    }

    private void updateWithMessage(Event<SuiteListener> message) {
        currentMessage = message;
        try {
            message.fireOn(suitePrinter);
        } finally {
            currentMessage = null;
        }
    }

    private void addTestName(String testClass, TestId testId, String name) {
        testNames.put(new GlobalTestId(testClass, testId), name);
    }

    private String getTestName(String testClass, TestId testId) {
        String name = testNames.get(new GlobalTestId(testClass, testId));
        assert name != null : "name not found for " + testClass + " and " + testId;
        return name;
    }

    private void visitRun(RunId runId, SuiteListener visitor) {
        for (Event<SuiteListener> event : runs.get(runId).events) {
            event.fireOn(visitor);
        }
    }

    // visual style
    // TODO: define all visual styles in one place (now parts is in RunPrinter)

    private void printSuiteFooter(int passCount, int failCount) {
        out.println(String.format("Pass: %d, Fail: %d, Total: %d", passCount, failCount, passCount + failCount));
    }


    @NotThreadSafe
    private static class RunState {
        private final String testClass;
        private final Deque<TestId> runningTests = new ArrayDeque<TestId>();
        private final List<Event<SuiteListener>> events = new ArrayList<Event<SuiteListener>>();

        private RunState(String testClass) {
            this.testClass = testClass;
        }
    }

    @NotThreadSafe
    private class SuitePrinter implements SuiteListener {

        @Override
        public void onSuiteStarted() {
        }

        @Override
        public void onTestFound(String testClass, TestId testId, String name) {
            addTestName(testClass, testId, name);
        }

        @Override
        public void onRunStarted(RunId runId, String testClass) {
            RunState run = new RunState(testClass);
            runs.put(runId, run);
            run.events.add(currentMessage);
        }

        @Override
        public void onTestStarted(RunId runId, TestId testId) {
            RunState run = runs.get(runId);
            run.events.add(currentMessage);

            run.runningTests.push(testId);
            allTests.add(new GlobalTestId(run.testClass, testId));
        }

        @Override
        public void onFailure(RunId runId, Throwable cause) {
            RunState run = runs.get(runId);
            run.events.add(currentMessage);

            TestId testId = run.runningTests.getLast();
            failedTests.add(new GlobalTestId(run.testClass, testId));
        }

        @Override
        public void onTestFinished(RunId runId) {
            RunState run = runs.get(runId);
            run.events.add(currentMessage);

            run.runningTests.pop(); // XXX: this line is not tested
        }

        @Override
        public void onRunFinished(RunId runId) {
            RunState run = runs.get(runId);
            run.events.add(currentMessage);

            // TODO: option for printing only failing or all runs
            // TODO: decouple printing from collecting the results
            visitRun(runId, new RunPrinter());
        }

        @Override
        public void onSuiteFinished() {
            int totalCount = allTests.size();
            int failCount = failedTests.size();
            int passCount = totalCount - failCount;

            // TODO: decouple printing from collecting the results
            printSuiteFooter(passCount, failCount);
            suiteFinished = true;
        }
    }

    @NotThreadSafe
    private class RunPrinter extends TestRunListener {
        // TODO: make static? (must first give access to getTestName)

        private final Deque<TestId> runningTests = new ArrayDeque<TestId>();
        private String testClass;

        @Override
        public void onRunStarted(RunId runId, String testClass) {
            this.testClass = testClass;
            printRunHeader(testClass, runId);
        }

        @Override
        public void onTestStarted(RunId runId, TestId testId) {
            printTestName("+", testClass, testId);
            runningTests.push(testId);
        }

        @Override
        public void onFailure(RunId runId, Throwable cause) {
            cause.printStackTrace(err);
        }

        @Override
        public void onTestFinished(RunId runId) {
            TestId testId = runningTests.pop();
            printTestName("-", testClass, testId);
        }

        @Override
        public void onRunFinished(RunId runId) {
            printRunFooter();
        }

        // visual style

        private void printRunHeader(String testClass, RunId runId) {
            out.println(" > Run #" + runId.toInt() + " in " + testClass);
        }

        private void printTestName(String bullet, String testClass, TestId testId) {
            out.println(" > " + testNameIndent() + bullet + " " + getTestName(testClass, testId));
        }

        private void printRunFooter() {
            out.println();
        }

        private String testNameIndent() {
            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < runningTests.size(); i++) {
                indent.append("  ");
            }
            return indent.toString();
        }
    }
}
