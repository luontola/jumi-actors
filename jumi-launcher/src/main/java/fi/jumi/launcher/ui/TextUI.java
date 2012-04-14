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

    private final Map<GlobalTestId, String> testNamesById = new HashMap<GlobalTestId, String>();
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

    private void addTestName(String testClass, TestId id, String name) {
        testNamesById.put(new GlobalTestId(testClass, id), name);
    }

    private String getTestName(String testClass, TestId id) {
        String name = testNamesById.get(new GlobalTestId(testClass, id));
        assert name != null : "name not found for " + testClass + " and " + id;
        return name;
    }

    private void createRun(RunId runId) {
        eventsByRunId.put(runId, new ArrayList<Event<SuiteListener>>());
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
            createRun(runId);
            addRunEvent(runId, currentMessage);
        }

        @Override
        public void onTestStarted(RunId runId, String testClass, TestId testId) {
            addRunEvent(runId, currentMessage);
            allTests.add(new GlobalTestId(testClass, testId));
        }

        @Override
        public void onFailure(RunId runId, String testClass, TestId testId, Throwable cause) {
            addRunEvent(runId, currentMessage);
            failedTests.add(new GlobalTestId(testClass, testId));
        }

        @Override
        public void onTestFinished(RunId runId, String testClass, TestId testId) {
            addRunEvent(runId, currentMessage);
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

            out.println(String.format("Pass: %d, Fail: %d, Total: %d", passCount, failCount, totalCount));

            suiteFinished = true;
        }
    }

    @NotThreadSafe
    private class RunPrinter extends TestRunListener {
        private int runningTests = 0;

        @Override
        public void onRunStarted(RunId runId, String testClass) {
            printRunHeader(testClass, runId);
        }

        @Override
        public void onTestStarted(RunId runId, String testClass, TestId testId) {
            printTestName("+", testClass, testId);
            runningTests++;
        }

        @Override
        public void onFailure(RunId runId, String testClass, TestId testId, Throwable cause) {
            cause.printStackTrace(err);
        }

        @Override
        public void onTestFinished(RunId runId, String testClass, TestId testId) {
            runningTests--;
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

        private void printTestName(String bullet, String testClass, TestId id) {
            out.println(" > " + testNameIndent() + bullet + " " + getTestName(testClass, id));
        }

        private void printRunFooter() {
            out.println();
        }

        private String testNameIndent() {
            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < runningTests; i++) {
                indent.append("  ");
            }
            return indent.toString();
        }
    }
}
