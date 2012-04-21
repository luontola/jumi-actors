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

    public void visitAllRuns(SuiteListener visitor) {
        for (RunId runId : runs.keySet()) {
            visitRun(runId, visitor);
        }
    }

    public void visitRun(RunId runId, SuiteListener visitor) {
        for (Event<SuiteListener> event : runs.get(runId).events) {
            event.fireOn(visitor);
        }
    }

    private void addTestName(String testClass, TestId testId, String name) {
        testNames.put(new GlobalTestId(testClass, testId), name);
    }

    public String getTestName(String testClass, TestId testId) {
        String name = testNames.get(new GlobalTestId(testClass, testId));
        assert name != null : "name not found for " + testClass + " and " + testId;
        return name;
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
        RunState run = new RunState();
        runs.put(runId, run);
        run.events.add(currentMessage);
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        RunState run = runs.get(runId);
        run.events.add(currentMessage);
    }

    @Override
    public void onFailure(RunId runId, Throwable cause) {
        RunState run = runs.get(runId);
        run.events.add(currentMessage);
    }

    @Override
    public void onTestFinished(RunId runId) {
        RunState run = runs.get(runId);
        run.events.add(currentMessage);
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
        private final List<Event<SuiteListener>> events = new ArrayList<Event<SuiteListener>>();
    }
}
