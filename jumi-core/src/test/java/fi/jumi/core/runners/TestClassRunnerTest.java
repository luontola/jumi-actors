// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import fi.jumi.core.events.*;
import fi.jumi.core.runs.RunIdSequence;
import org.junit.Test;

import java.util.concurrent.Executor;

public class TestClassRunnerTest {

    private final SpyListener<TestClassRunnerListener> spy = new SpyListener<TestClassRunnerListener>(TestClassRunnerListener.class);
    private final TestClassRunnerListener expect = spy.getListener();
    private final SingleThreadedActors actors = new SingleThreadedActors(
            new StartableFactory(),
            new RunnableFactory(),
            new ExecutorFactory(),
            new ExecutorListenerFactory(),
            new TestClassListenerFactory()
    );

    @Test
    public void notifies_when_the_test_class_is_finished() {
        // TODO: these expectations are not interesting for this test - find a way to write this test without mentioning them
        expect.onTestFound(TestId.ROOT, "root test");
        expect.onTestFound(TestId.of(0), "test one");
        expect.onTestStarted(TestId.of(0));
        expect.onTestFinished(TestId.of(0));
        expect.onTestFound(TestId.of(1), "test two");
        expect.onTestStarted(TestId.of(1));
        expect.onTestFinished(TestId.of(1));

        // this must happen last, once
        expect.onTestClassFinished();

        runAndCheckExpectations(new TwoTestsDriver());
    }

    // TODO: forwards_all_other_events - find a way to write this as unit test


    // helpers

    private void runAndCheckExpectations(Driver driver) {
        RunIdSequence runIdSequence = new RunIdSequence();
        TestClassRunner runner = new TestClassRunner(DummyTest.class, driver, expect, actors, actors.getExecutor(), runIdSequence);

        spy.replay();
        actors.createPrimaryActor(Startable.class, runner, "TestClassRunner").start();
        actors.processEventsUntilIdle();
        spy.verify();
    }


    // guinea pigs

    private static class DummyTest {
    }

    public static class TwoTestsDriver implements Driver {
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "root test");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    notifier.fireTestFound(TestId.of(0), "test one");
                    notifier.fireTestStarted(TestId.of(0))
                            .fireTestFinished();
                }
            });
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    notifier.fireTestFound(TestId.of(1), "test two");
                    notifier.fireTestStarted(TestId.of(1))
                            .fireTestFinished();
                }
            });
        }
    }
}
