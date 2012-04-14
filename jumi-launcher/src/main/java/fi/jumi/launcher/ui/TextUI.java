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

    // TODO: extract run context into its own class
    private final Map<GlobalTestId, String> testNamesById = new HashMap<GlobalTestId, String>();
    private final Map<RunId, String> testClassesByRunId = new HashMap<RunId, String>();
    private final Map<RunId, Deque<TestId>> runningTestsByRunId = new HashMap<RunId, Deque<TestId>>();
    private final Map<RunId, List<Event<SuiteListener>>> eventsByRunId = new HashMap<RunId, List<Event<SuiteListener>>>();

    // TODO: extract counting to its own class
    private final Set<GlobalTestId> failedTests = new HashSet<GlobalTestId>();
    private final Set<GlobalTestId> allTests = new HashSet<GlobalTestId>();

    private EventCollector eventCollector = new EventCollector();
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
            message.fireOn(eventCollector);
        } finally {
            currentMessage = null;
        }
    }

    private void addTestName(String testClass, TestId testId, String name) {
        testNamesById.put(new GlobalTestId(testClass, testId), name);
    }

    private String getTestName(String testClass, TestId testId) {
        String name = testNamesById.get(new GlobalTestId(testClass, testId));
        assert name != null : "name not found for " + testClass + " and " + testId;
        return name;
    }

    private void createRun(RunId runId, String testClass) {
        runningTestsByRunId.put(runId, new ArrayDeque<TestId>());
        eventsByRunId.put(runId, new ArrayList<Event<SuiteListener>>());
        testClassesByRunId.put(runId, testClass);
    }

    private void addRunEvent(RunId runId, Event<SuiteListener> event) {
        eventsByRunId.get(runId).add(event);
    }

    private void visitRun(RunId runId, SuiteListener visitor) {
        List<Event<SuiteListener>> events = eventsByRunId.get(runId);
        for (Event<SuiteListener> event : events) {
            event.fireOn(visitor);
        }
    }

    // visual style
    // TODO: define all visual styles in one place (now parts is in RunPrinter)

    private void printSuiteFooter(int passCount, int failCount) {
        out.println(String.format("Pass: %d, Fail: %d, Total: %d", passCount, failCount, passCount + failCount));
    }


    @NotThreadSafe
    private class EventCollector implements SuiteListener {

        @Override
        public void onSuiteStarted() {
        }

        @Override
        public void onTestFound(String testClass, TestId testId, String name) {
            addTestName(testClass, testId, name);
        }

        @Override
        public void onRunStarted(RunId runId, String testClass) {
            createRun(runId, testClass);
            addRunEvent(runId, currentMessage);
        }

        @Override
        public void onTestStarted(RunId runId, TestId testId) {
            addRunEvent(runId, currentMessage);
            String testClass = testClassesByRunId.get(runId);
            allTests.add(new GlobalTestId(testClass, testId));
            runningTestsByRunId.get(runId).push(testId);
        }

        @Override
        public void onFailure(RunId runId, Throwable cause) {
            addRunEvent(runId, currentMessage);
            String testClass = testClassesByRunId.get(runId);
            TestId testId = runningTestsByRunId.get(runId).getLast();
            failedTests.add(new GlobalTestId(testClass, testId));
        }

        @Override
        public void onTestFinished(RunId runId) {
            addRunEvent(runId, currentMessage);
            runningTestsByRunId.get(runId).poll();
        }

        @Override
        public void onRunFinished(RunId runId) {
            addRunEvent(runId, currentMessage);
            // TODO: option for printing only failing or all runs
            visitRun(runId, new RunPrinter());
        }

        @Override
        public void onSuiteFinished() {
            int totalCount = allTests.size();
            int failCount = failedTests.size();
            int passCount = totalCount - failCount;

            printSuiteFooter(passCount, failCount);
            suiteFinished = true;
        }
    }

    @NotThreadSafe
    private class RunPrinter extends TestRunListener {

        private final Deque<TestId> runningTests = new ArrayDeque<TestId>();

        @Override
        public void onRunStarted(RunId runId, String testClass) {
            printRunHeader(testClass, runId);
        }

        @Override
        public void onTestStarted(RunId runId, TestId testId) {
            String testClass = testClassesByRunId.get(runId);
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
            String testClass = testClassesByRunId.get(runId);
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
