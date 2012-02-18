// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.Event;
import fi.jumi.actors.MessageReceiver;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.events.suite.OnFailureEvent;
import fi.jumi.core.events.suite.OnTestFinishedEvent;
import fi.jumi.core.events.suite.OnTestStartedEvent;

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
    private final Map<Integer, List<Event<SuiteListener>>> eventsByRunId = new HashMap<Integer, List<Event<SuiteListener>>>();

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

    private void addRunEvent(int runId, Event<SuiteListener> event) {
        List<Event<SuiteListener>> events = eventsByRunId.get(runId);
        if (events == null) {
            events = new ArrayList<Event<SuiteListener>>();
            eventsByRunId.put(runId, events);
        }
        events.add(event);
    }

    private boolean isRunFinished(int runId) {
        RunStatusEvaluator runStatus = new RunStatusEvaluator();

        List<Event<SuiteListener>> events = eventsByRunId.get(runId);
        for (Event<SuiteListener> event : events) {
            event.fireOn(runStatus);
        }

        return runStatus.isRunFinished();
    }

    private void printRun(int runId) {
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

        out.println();
        out.println(String.format("Pass: %d, Fail: %d, Total: %d", passCount, failCount, totalCount));

        suiteFinished = true;
    }

    @Override
    public void onTestFound(String testClass, TestId id, String name) {
        addTestName(testClass, id, name);
    }

    @Override
    public void onTestStarted(int runId, String testClass, TestId id) {
        addRunEvent(runId, new OnTestStartedEvent(runId, testClass, id));

        totalCount.add(new GlobalTestId(testClass, id));
    }

    @Override
    public void onTestFinished(int runId, String testClass, TestId id) {
        addRunEvent(runId, new OnTestFinishedEvent(runId, testClass, id));

        // TODO: option for printing only failing or all runs
        if (isRunFinished(runId)) {
            printRun(runId);
        }
    }

    @Override
    public void onFailure(int runId, String testClass, TestId id, Throwable cause) {
        addRunEvent(runId, new OnFailureEvent(runId, testClass, id, cause));

        failCount.add(new GlobalTestId(testClass, id));
    }


    @NotThreadSafe
    private class RunPrinter extends TestRunListener {
        private int runningTests = 0;

        public void onTestStarted(int runId, String testClass, TestId id) {
            if (runningTests == 0) {
                printRunHeader(testClass, runId);
            }
            printTestName("+", testClass, id);
            runningTests++;
        }

        public void onTestFinished(int runId, String testClass, TestId id) {
            runningTests--;
            printTestName("-", testClass, id);
        }

        public void onFailure(int runId, String testClass, TestId id, Throwable cause) {
            cause.printStackTrace(err);
        }

        // visual style

        private void printRunHeader(String testClass, int runId) {
            out.println(" > Run #" + runId + " in " + testClass);
        }

        private void printTestName(String bullet, String testClass, TestId id) {
            out.println(" > " + testNameIndent() + bullet + " " + getTestName(testClass, id));
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
    private class RunStatusEvaluator extends TestRunListener {
        private int runningTests = 0;

        public boolean isRunFinished() {
            // XXX: should not be finished before it is started (i.e. there are zero events)
            return runningTests == 0;
        }

        public void onTestStarted(int runId, String testClass, TestId id) {
            runningTests++;
        }

        public void onTestFinished(int runId, String testClass, TestId id) {
            runningTests--;
        }

        public void onFailure(int runId, String testClass, TestId id, Throwable cause) {
        }
    }
}

