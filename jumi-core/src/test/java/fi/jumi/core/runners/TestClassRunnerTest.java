// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.Startable;
import fi.jumi.core.events.*;
import fi.jumi.core.runs.*;
import fi.jumi.core.utils.MethodCallSpy;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class TestClassRunnerTest {

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new StartableFactory(),
            new RunnableFactory(),
            new ExecutorFactory(),
            new ExecutorListenerFactory(),
            new TestClassListenerFactory()
    );

    private final MethodCallSpy spy = new MethodCallSpy();
    private final TestClassRunnerListener listener = spy.createProxyTo(TestClassRunnerListener.class);

    @Test
    public void notifies_when_the_test_class_is_finished() {
        run(new TwoTestsDriver());

        assertThat("should happen once", spy.countCallsTo("onTestClassFinished"), is(1));
        assertThat("should happen last", spy.getLastCall(), is("onTestClassFinished"));
    }

    @Test
    public void forwards_all_other_events() {
        TestClassRunnerListener target = mock(TestClassRunnerListener.class);
        TestClassRunner runner = new TestClassRunner(null, null, target, null, null, null);
        final TestId TEST_ID = TestId.of(1);
        final String NAME = "name";
        final RunId RUN_ID = new RunId(1);
        final Throwable CAUSE = new Throwable();

        runner.onTestFound(TEST_ID, NAME);
        verify(target).onTestFound(TEST_ID, NAME);

        runner.onRunStarted(RUN_ID);
        verify(target).onRunStarted(RUN_ID);

        runner.onTestStarted(RUN_ID, TEST_ID);
        verify(target).onTestStarted(RUN_ID, TEST_ID);

        runner.onFailure(RUN_ID, TEST_ID, CAUSE);
        verify(target).onFailure(RUN_ID, TEST_ID, CAUSE);

        runner.onTestFinished(RUN_ID, TEST_ID);
        verify(target).onTestFinished(RUN_ID, TEST_ID);

        runner.onRunFinished(RUN_ID);
        verify(target).onRunFinished(RUN_ID);

        verifyNoMoreInteractions(target);
    }


    // helpers

    private void run(Driver driver) {
        RunIdSequence runIdSequence = new RunIdSequence();
        TestClassRunner runner = new TestClassRunner(DummyTest.class, driver, listener, actors, actors.getExecutor(), runIdSequence);

        actors.createPrimaryActor(Startable.class, runner, "TestClassRunner").start();
        actors.processEventsUntilIdle();
    }


    // guinea pigs

    private static class DummyTest {
    }

    public static class TwoTestsDriver implements Driver {
        @Override
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
