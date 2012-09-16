// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.simpleunit;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.runners.TestClassListener;
import fi.jumi.core.runs.*;
import fi.jumi.core.util.SpyListener;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import sample.*;

import java.nio.charset.Charset;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;

public class SimpleUnitTest {

    private static final long TIMEOUT = 1000;

    private static final RunId RUN_1 = new RunId(1);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SimpleUnit driver = new SimpleUnit();

    @Test
    public void the_test_class_is_named_after_its_simple_name() throws InterruptedException {
        TestClassListener listener = mock(TestClassListener.class);

        executeTestClass(OnePassingTest.class, listener);

        verify(listener).onTestFound(TestId.ROOT, "OnePassingTest");
    }

    @Test
    public void the_tests_are_methods_whose_name_starts_with_test() throws InterruptedException {
        TestClassListener listener = mock(TestClassListener.class);

        executeTestClass(OnePassingTest.class, listener);

        verify(listener).onTestFound(TestId.of(0), "testPassing");
    }

    @Test
    public void reports_test_execution() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "OnePassingTest");
        listener.onTestFound(TestId.of(0), "testPassing");

        listener.onRunStarted(RUN_1);
        listener.onTestStarted(RUN_1, TestId.ROOT);
        listener.onTestStarted(RUN_1, TestId.of(0));
        listener.onTestFinished(RUN_1, TestId.of(0));
        listener.onTestFinished(RUN_1, TestId.ROOT);
        listener.onRunFinished(RUN_1);

        spy.replay();
        executeTestClass(OnePassingTest.class, listener);
        spy.verify();
    }

    @Test
    public void reports_test_failure() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "OneFailingTest");
        listener.onTestFound(TestId.of(0), "testFailing");

        listener.onRunStarted(RUN_1);
        listener.onTestStarted(RUN_1, TestId.ROOT);
        listener.onTestStarted(RUN_1, TestId.of(0));
        listener.onFailure(RUN_1, TestId.of(0), new AssertionError("dummy failure"));
        listener.onTestFinished(RUN_1, TestId.of(0));
        listener.onTestFinished(RUN_1, TestId.ROOT);
        listener.onRunFinished(RUN_1);

        spy.replay();
        executeTestClass(OneFailingTest.class, listener);
        spy.verify();
    }

    @Test
    public void reports_failures_in_constructor() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "FailureInConstructorTest");
        listener.onTestFound(TestId.of(0), "testNotExecuted");

        listener.onRunStarted(RUN_1);
        listener.onTestStarted(RUN_1, TestId.ROOT);
        listener.onFailure(RUN_1, TestId.ROOT, new RuntimeException("dummy exception"));
        listener.onTestFinished(RUN_1, TestId.ROOT);
        listener.onRunFinished(RUN_1);

        spy.replay();
        executeTestClass(FailureInConstructorTest.class, listener);
        spy.verify();
    }

    @Test
    public void reports_illegal_test_method_signatures() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "IllegalTestMethodSignatureTest");
        listener.onTestFound(TestId.of(0), "testMethodWithParameters");

        listener.onRunStarted(RUN_1);
        listener.onTestStarted(RUN_1, TestId.ROOT);
        listener.onTestStarted(RUN_1, TestId.of(0));
        listener.onFailure(RUN_1, TestId.of(0), new IllegalArgumentException("wrong number of arguments"));
        listener.onTestFinished(RUN_1, TestId.of(0));
        listener.onTestFinished(RUN_1, TestId.ROOT);
        listener.onRunFinished(RUN_1);

        spy.replay();
        executeTestClass(IllegalTestMethodSignatureTest.class, listener);
        spy.verify();
    }

    @Test
    public void reports_an_error_if_the_test_class_contains_no_test_methods() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "NoTestMethodsTest");
        listener.onRunStarted(RUN_1);
        listener.onTestStarted(RUN_1, TestId.ROOT);
        listener.onFailure(RUN_1, TestId.ROOT, new IllegalArgumentException("No test methods in class fi.jumi.test.simpleunit.NoTestMethodsTest"));
        listener.onTestFinished(RUN_1, TestId.ROOT);
        listener.onRunFinished(RUN_1);

        spy.replay();
        executeTestClass(NoTestMethodsTest.class, listener);
        spy.verify();
    }


    // helpers

    private void executeTestClass(Class<?> testClass, TestClassListener listener) throws InterruptedException {
        OutputCapturer outputCapturer = new OutputCapturer(new NullOutputStream(), new NullOutputStream(), Charset.defaultCharset());
        driver.findTests(testClass, new DefaultSuiteNotifier(ActorRef.wrap(listener), new RunIdSequence(), outputCapturer), executor);
        waitForTestsToExecute();
    }

    private void waitForTestsToExecute() throws InterruptedException {
        // XXX: for a different kind of testing framework, shutdown() won't work, because it prevents new tasks from being added
        executor.shutdown();
        executor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
    }

    // TODO: these tests will probably need to be rewritten; they should also serve as an example of how to write tests for a testing framework

    // TODO: consider how to write these tests
    // It should not be necessary to assert timestamps (once all the events have them),
    // so it might make sense to inject into DefaultSuiteNotifier a class which
    // adds the timestamps and forwards the events to an timestampful interface.
}
