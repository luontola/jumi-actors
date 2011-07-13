// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.*;
import net.orfjackal.jumi.core.actors.ThreadedActors;
import net.orfjackal.jumi.core.dynamicevents.DynamicListenerFactory;
import org.junit.*;
import org.mockito.InOrder;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

public class TestClassRunnerTest {

    private final SuiteListener listener = mock(SuiteListener.class);
    private final InOrder inOrder = inOrder(listener);

    private final ThreadedActors actors = new ThreadedActors(DynamicListenerFactory.factoriesFor(Runnable.class));

    @After
    public void shutdownThreadPool() throws InterruptedException {
        actors.shutdown(1000);
    }

    @Test
    public void test_class_with_zero_tests() throws Exception {
        TestClassRunner runner = new TestClassRunner(DummyTest.class, ZeroTestsDriver.class, listener, actors);

        runAndAwaitCompletion(runner);

        // TODO: is this an allowed situation? in practice it means that the class is not reported anywhere
        inOrder.verify(listener).onTestClassStarted(DummyTest.class);
        inOrder.verify(listener).onTestClassFinished(DummyTest.class);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void test_class_with_only_root_test() throws Exception {
        TestClassRunner runner = new TestClassRunner(DummyTest.class, OneTestDriver.class, listener, actors);

        runAndAwaitCompletion(runner);

        inOrder.verify(listener).onTestClassStarted(DummyTest.class);
        inOrder.verify(listener).onTestFound(DummyTest.class.getName(), TestId.ROOT, "root test");
        inOrder.verify(listener).onTestClassFinished(DummyTest.class);
        verifyNoMoreInteractions(listener);
    }

    private void runAndAwaitCompletion(TestClassRunner runner) throws InterruptedException {
        Runnable handle = actors.startEventPoller(Runnable.class, runner, "TestClassRunner");
        handle.run();       // command to start up test class execution
        Thread.sleep(100);  // XXX
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
