// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.*;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.events.suiteListener.*;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.PrintStream;
import java.util.*;

@NotThreadSafe
public class TextUI implements SuiteListener {

    private final PrintStream out;
    private final PrintStream err;

    // TODO: if multiple readers are needed, create a Streamer class per the original designs
    private final MessageReceiver<Event<SuiteListener>> eventStream;
    private boolean suiteFinished = false;

    private final Map<GlobalTestId, String> testNamesById = new HashMap<GlobalTestId, String>();
    private final Map<RunId, List<Event<SuiteListener>>> eventsByRunId = new HashMap<RunId, List<Event<SuiteListener>>>();

    // TODO: extract counting to its own class
    private final Set<GlobalTestId> failCount = new HashSet<GlobalTestId>();
    private final Set<GlobalTestId> totalCount = new HashSet<GlobalTestId>();

    public TextUI(PrintStream out, PrintStream err, MessageReceiver<Event<SuiteListener>> eventStream) {
        this.out = out;
        this.err = err;
        this.eventStream = eventStream;
    }

    private void addTestName(String testClass, TestId id, String name) {
        testNamesById.put(new GlobalTestId(testClass, id), name);
    }

    private String getTestName(String testClass, TestId id) {
        String name = testNamesById.get(new GlobalTestId(testClass, id));
        assert name != null : "name not found for " + testClass + " and " + id;
        return name;
    }

    private void addRunEvent(RunId runId, Event<SuiteListener> event) {
        List<Event<SuiteListener>> events = eventsByRunId.get(runId);
        events.add(event);
    }

    private boolean isRunFinished(RunId runId) {
        RunStatusEvaluator runStatus = new RunStatusEvaluator();

        // TODO: extract "visit(runId, visitor): visitor"
        List<Event<SuiteListener>> events = eventsByRunId.get(runId);
        for (Event<SuiteListener> event : events) {
            event.fireOn(runStatus);
        }

        return runStatus.isRunFinished();
    }

    private void printRun(RunId runId) {
        RunPrinter printer = new RunPrinter();

        List<Event<SuiteListener>> events = eventsByRunId.get(runId);
        for (Event<SuiteListener> event : events) {
            event.fireOn(printer);
        }
    }

    public void update() {
        while (!suiteFinished) {
            Event<SuiteListener> message = eventStream.poll();
            if (message == null) {
                break;
            }
            message.fireOn(this);
        }
    }

    public void updateUntilFinished() throws InterruptedException {
        while (!suiteFinished) {
            Event<SuiteListener> message = eventStream.take();
            message.fireOn(this);
        }
    }

    // SuiteListener

    @Override
    public void onSuiteStarted() {
    }

    @Override
    public void onSuiteFinished() {
        int totalCount = this.totalCount.size();
        int failCount = this.failCount.size();
        int passCount = totalCount - failCount;

        out.println(String.format("Pass: %d, Fail: %d, Total: %d", passCount, failCount, totalCount));

        suiteFinished = true;
    }

    @Override
    public void onTestFound(String testClass, TestId testId, String name) {
        addTestName(testClass, testId, name);
    }

    @Override
    public void onRunStarted(RunId runId, String testClass) {
        eventsByRunId.put(runId, new ArrayList<Event<SuiteListener>>());
        addRunEvent(runId, new OnRunStartedEvent(runId, testClass));
    }

    @Override
    public void onTestStarted(RunId runId, String testClass, TestId testId) {
        addRunEvent(runId, new OnTestStartedEvent(runId, testClass, testId));

        totalCount.add(new GlobalTestId(testClass, testId));
    }

    @Override
    public void onFailure(RunId runId, String testClass, TestId testId, Throwable cause) {
        addRunEvent(runId, new OnFailureEvent(runId, testClass, testId, cause));

        failCount.add(new GlobalTestId(testClass, testId));
    }

    @Override
    public void onTestFinished(RunId runId, String testClass, TestId testId) {
        addRunEvent(runId, new OnTestFinishedEvent(runId, testClass, testId));
    }

    @Override
    public void onRunFinished(RunId runId) {
        addRunEvent(runId, new OnRunFinishedEvent(runId));

        // TODO: option for printing only failing or all runs
        printRun(runId);
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

    @NotThreadSafe
    private class RunStatusEvaluator extends TestRunListener { // TODO: extract NullTestRunListener, only onRunFinished is needed here
        private int runningTests = 0;

        public boolean isRunFinished() {
            // XXX: should not be finished before it is started (i.e. there are zero events)
            return runningTests == 0;
        }

        @Override
        public void onRunStarted(RunId runId, String testClass) {
            // TODO
        }

        @Override
        public void onTestStarted(RunId runId, String testClass, TestId testId) {
            runningTests++;
        }

        @Override
        public void onFailure(RunId runId, String testClass, TestId testId, Throwable cause) {
            // ignore - doesn't affect run status
        }

        @Override
        public void onTestFinished(RunId runId, String testClass, TestId testId) {
            runningTests--;
        }

        @Override
        public void onRunFinished(RunId runId) {
            // TODO
        }
    }
}
