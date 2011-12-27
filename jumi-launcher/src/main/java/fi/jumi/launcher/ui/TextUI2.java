// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.Event;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.events.suite.OnFailureEvent;
import fi.jumi.core.events.suite.OnTestFinishedEvent;
import fi.jumi.core.events.suite.OnTestStartedEvent;

import java.io.PrintStream;
import java.util.*;

public class TextUI2 implements SuiteListener {

    private final PrintStream out;
    private final PrintStream err;
    private final FakeStream eventStream;

    private Map<Integer, List<Event<SuiteListener>>> eventsByRunId = new HashMap<Integer, List<Event<SuiteListener>>>();

    // TODO: extract counting to its own class
    private final Set<GlobalTestId> failCount = new HashSet<GlobalTestId>();
    private final Set<GlobalTestId> totalCount = new HashSet<GlobalTestId>();

    public TextUI2(PrintStream out, PrintStream err, FakeStream eventStream) {
        this.out = out;
        this.err = err;
        this.eventStream = eventStream;
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
        while (true) {
            Event<SuiteListener> message = eventStream.poll();
            if (message == null) {
                break;
            }
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
    }

    @Override
    public void onTestFound(String testClass, TestId id, String name) {
    }

    @Override
    public void onTestStarted(String testClass, TestId id) {
        int runId = 42; // TODO: get Run IDs from the test runner

        addRunEvent(runId, new OnTestStartedEvent(testClass, id));

        totalCount.add(new GlobalTestId(testClass, id));
    }

    @Override
    public void onTestFinished(String testClass, TestId id) {
        int runId = 42; // TODO: get Run IDs from the test runner

        addRunEvent(runId, new OnTestFinishedEvent(testClass, id));
        if (isRunFinished(runId)) {
            printRun(runId);
        }
    }

    @Override
    public void onFailure(String testClass, TestId id, Throwable cause) {
        int runId = 42; // TODO: get Run IDs from the test runner

        addRunEvent(runId, new OnFailureEvent(testClass, id, cause));

        failCount.add(new GlobalTestId(testClass, id));
    }

    private class RunPrinter extends TestRunListener {

        public void onTestStarted(String testClass, TestId id) {
        }

        public void onTestFinished(String testClass, TestId id) {
        }

        public void onFailure(String testClass, TestId id, Throwable cause) {
            cause.printStackTrace(err);
        }
    }

    private class RunStatusEvaluator extends TestRunListener {
        private int runningTests = 0;

        public boolean isRunFinished() {
            // XXX: should not be finished before it is started (i.e. there are zero events)
            return runningTests == 0;
        }

        public void onTestStarted(String testClass, TestId id) {
            runningTests++;
        }

        public void onTestFinished(String testClass, TestId id) {
            runningTests--;
        }

        public void onFailure(String testClass, TestId id, Throwable cause) {
        }
    }
}

