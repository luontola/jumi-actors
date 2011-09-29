// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.Startable;
import fi.jumi.core.events.runnable.RunnableFactory;
import fi.jumi.core.events.startable.StartableFactory;
import fi.jumi.core.events.testclass.TestClassListenerFactory;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

public class TestClassRunnerTest {

    private final TestClassRunnerListener listener = mock(TestClassRunnerListener.class);
    private final InOrder inOrder = inOrder(listener);

    private final SingleThreadedActors actors = new SingleThreadedActors(
            new StartableFactory(),
            new RunnableFactory(),
            new TestClassListenerFactory()
    );

    @Test
    public void test_class_with_zero_tests() {
        TestClassRunner runner = new TestClassRunner(DummyTest.class, ZeroTestsDriver.class, listener, actors);

        runAndAwaitCompletion(runner);

        // TODO: is this an allowed situation? in practice it means that the class is not reported anywhere
        inOrder.verify(listener).onTestClassFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void test_class_with_only_root_test() {
        TestClassRunner runner = new TestClassRunner(DummyTest.class, OneTestDriver.class, listener, actors);

        runAndAwaitCompletion(runner);

        inOrder.verify(listener).onTestFound(TestId.ROOT, "root test");
        inOrder.verify(listener).onTestClassFinished();
        verifyNoMoreInteractions(listener);
    }

    private void runAndAwaitCompletion(TestClassRunner runner) {
        actors.createPrimaryActor(Startable.class, runner, "TestClassRunner").start();
        actors.processEventsUntilIdle();
    }


    private static class DummyTest {
    }

    static class ZeroTestsDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }

    static class OneTestDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "root test");
        }
    }
}
