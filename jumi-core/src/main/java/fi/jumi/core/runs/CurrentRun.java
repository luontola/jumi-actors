// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.output.*;
import fi.jumi.core.runners.TestClassListener;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
class CurrentRun {

    private final ActorRef<TestClassListener> listener;
    private final RunIdSequence runIdSequence;
    private final OutputCapturer outputCapturer;

    private final InheritableThreadLocal<RunContext> currentRun = new InheritableThreadLocal<>();

    public CurrentRun(ActorRef<TestClassListener> listener, RunIdSequence runIdSequence, OutputCapturer outputCapturer) {
        this.listener = listener;
        this.runIdSequence = runIdSequence;
        this.outputCapturer = outputCapturer;
    }

    public void fireTestFound(TestId testId, String name) {
        listener.tell().onTestFound(testId, name);
    }

    public void fireTestStarted(TestId testId) {
        RunContext currentRun = this.currentRun.get();

        // notify run started?
        if (currentRun == null) {
            currentRun = new RunContext(runIdSequence.nextRunId());
            this.currentRun.set(currentRun);
            fireRunStarted(currentRun);
        }

        // notify test started
        currentRun.countTestStarted();
        listener.tell().onTestStarted(currentRun.runId, testId);
    }

    public void fireTestFinished(TestId testId) {
        RunContext currentRun = this.currentRun.get();

        // notify test finished
        currentRun.countTestFinished();
        listener.tell().onTestFinished(currentRun.runId, testId);

        // notify run finished?
        if (currentRun.isRunFinished()) {
            this.currentRun.remove();
            fireRunFinished(currentRun);
        }
    }

    private void fireRunStarted(RunContext currentRun) {
        listener.tell().onRunStarted(currentRun.runId);
        outputCapturer.captureTo(new OutputListenerAdapter(listener, currentRun.runId));
    }

    private void fireRunFinished(RunContext currentRun) {
        outputCapturer.captureTo(new NullOutputListener());
        listener.tell().onRunFinished(currentRun.runId);
    }

    public void fireFailure(TestId testId, Throwable cause) {
        RunContext currentRun = this.currentRun.get();
        listener.tell().onFailure(currentRun.runId, testId, cause);
    }


    @ThreadSafe
    private static class RunContext {
        public final RunId runId;
        private final AtomicInteger testNestingLevel = new AtomicInteger(0);

        public RunContext(RunId runId) {
            this.runId = runId;
        }

        public void countTestStarted() {
            testNestingLevel.incrementAndGet();
        }

        public void countTestFinished() {
            int level = testNestingLevel.decrementAndGet();
            assert level >= 0;
        }

        public boolean isRunFinished() {
            return testNestingLevel.get() == 0;
        }
    }

    @ThreadSafe
    private static class OutputListenerAdapter implements OutputListener {
        private final ActorRef<TestClassListener> listener;
        private final RunId runId;

        public OutputListenerAdapter(ActorRef<TestClassListener> listener, RunId runId) {
            this.listener = listener;
            this.runId = runId;
        }

        @Override
        public void out(String text) {
            listener.tell().onPrintedOut(runId, text);
        }

        @Override
        public void err(String text) {
            listener.tell().onPrintedErr(runId, text);
        }
    }
}
