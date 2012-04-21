// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.*;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

public class SuiteEventDemuxer implements SuiteListener, MessageSender<Event<SuiteListener>> {

    private final Map<GlobalTestId, String> testNames = new HashMap<GlobalTestId, String>();
    private final Map<RunId, RunState> runs = new HashMap<RunId, RunState>();

    private Event<SuiteListener> currentMessage;
    private boolean suiteFinished = false;

    private TextUI textUI; // XXX: remove me

    public SuiteEventDemuxer(TextUI textUI) {
        this.textUI = textUI;
    }

    @Override
    public void send(Event<SuiteListener> message) {
        currentMessage = message;
        try {
            message.fireOn(this);
        } finally {
            currentMessage = null;
        }
    }

    public boolean isSuiteFinished() {
        return suiteFinished;
    }

    private void addTestName(String testClass, TestId testId, String name) {
        testNames.put(new GlobalTestId(testClass, testId), name);
    }

    public String getTestName(String testClass, TestId testId) {
        String name = testNames.get(new GlobalTestId(testClass, testId));
        assert name != null : "name not found for " + testClass + " and " + testId;
        return name;
    }

    public void visitRun(RunId runId, SuiteListener visitor) {
        for (Event<SuiteListener> event : runs.get(runId).events) {
            event.fireOn(visitor);
        }
    }

    // TODO: make SuiteListener methods private

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
        textUI.allTests.add(new GlobalTestId(run.testClass, testId));
    }

    @Override
    public void onFailure(RunId runId, Throwable cause) {
        RunState run = runs.get(runId);
        run.events.add(currentMessage);

        TestId testId = run.runningTests.getLast();
        textUI.failedTests.add(new GlobalTestId(run.testClass, testId));
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
    }

    @Override
    public void onSuiteFinished() {
        suiteFinished = true;
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
}
