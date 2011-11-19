// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.simpleunit;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.runners.*;
import org.junit.Test;
import sample.*;

import java.util.Collection;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class SimpleUnitTest {
    private static final long TIMEOUT = 1000;

    private TestClassRunnerListener dummyListener = mock(TestClassRunnerListener.class);
    private ExecutorService executor = Executors.newCachedThreadPool();
    private SimpleUnit driver = new SimpleUnit();

    private TestClassRunner results = new TestClassRunner(null, null, dummyListener, null, null); // XXX: use something else to collect the results?

    @Test
    public void the_test_class_is_named_after_its_simple_name() throws InterruptedException {
        executeTestClass(OnePassingTest.class, results);

        assertThat(results.getTestNames(), hasItem("OnePassingTest"));
    }

    @Test
    public void the_tests_are_methods_whose_name_starts_with_test() throws InterruptedException {
        executeTestClass(OnePassingTest.class, results);

        Collection<String> testNames = results.getTestNames();
        assertThat(testNames, hasItem("testPassing"));
        assertThat(testNames.size(), is(2)); // one root plus the one passing test
    }

    @Test
    public void reports_test_execution() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<TestClassListener>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "OnePassingTest");
        listener.onTestFound(TestId.of(0), "testPassing");

        listener.onTestStarted(TestId.ROOT);
        listener.onTestStarted(TestId.of(0));
        listener.onTestFinished(TestId.of(0));
        listener.onTestFinished(TestId.ROOT);

        spy.replay();
        executeTestClass(OnePassingTest.class, listener);
        spy.verify();
    }

    @Test
    public void reports_test_failure() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<TestClassListener>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "OneFailingTest");
        listener.onTestFound(TestId.of(0), "testFailing");

        listener.onTestStarted(TestId.ROOT);
        listener.onTestStarted(TestId.of(0));
        listener.onFailure(TestId.of(0), new AssertionError("dummy failure"));
        listener.onTestFinished(TestId.of(0));
        listener.onTestFinished(TestId.ROOT);

        spy.replay();
        executeTestClass(OneFailingTest.class, listener);
        spy.verify();
    }

    @Test
    public void reports_failures_in_constructor() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<TestClassListener>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "FailureInConstructorTest");
        listener.onTestFound(TestId.of(0), "testNotExecuted");

        listener.onTestStarted(TestId.ROOT);
        listener.onFailure(TestId.ROOT, new RuntimeException("dummy exception"));
        listener.onTestFinished(TestId.ROOT);

        spy.replay();
        executeTestClass(FailureInConstructorTest.class, listener);
        spy.verify();
    }

    @Test
    public void reports_illegal_test_method_signatures() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<TestClassListener>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "IllegalTestMethodSignatureTest");
        listener.onTestFound(TestId.of(0), "testMethodWithParameters");

        listener.onTestStarted(TestId.ROOT);
        listener.onTestStarted(TestId.of(0));
        listener.onFailure(TestId.of(0), new IllegalArgumentException("wrong number of arguments"));
        listener.onTestFinished(TestId.of(0));
        listener.onTestFinished(TestId.ROOT);

        spy.replay();
        executeTestClass(IllegalTestMethodSignatureTest.class, listener);
        spy.verify();
    }

    @Test
    public void reports_an_error_if_the_test_class_contains_no_test_methods() throws InterruptedException {
        SpyListener<TestClassListener> spy = new SpyListener<TestClassListener>(TestClassListener.class);
        TestClassListener listener = spy.getListener();

        listener.onTestFound(TestId.ROOT, "NoTestMethodsTest");
        listener.onTestStarted(TestId.ROOT);
        listener.onFailure(TestId.ROOT, new IllegalArgumentException("No test methods in class fi.jumi.test.simpleunit.NoTestMethodsTest"));
        listener.onTestFinished(TestId.ROOT);

        spy.replay();
        executeTestClass(NoTestMethodsTest.class, listener);
        spy.verify();
    }


    // helpers

    private void executeTestClass(Class<?> testClass, TestClassListener listener) throws InterruptedException {
        driver.findTests(testClass, new DefaultSuiteNotifier(listener), executor);
        waitForTestsToExecute();
    }

    private void waitForTestsToExecute() throws InterruptedException {
        // XXX: for a differnt kind of testing framework, shutdown() won't work, because it prevents new tasks from being added
        executor.shutdown();
        executor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
    }

    // TODO: these tests will probably need to be rewritten; they should also serve as an example of how to write tests for a testing framework

    // TODO: consider how to write these tests
    // It should not be necessary to assert timestamps (once all the events have them),
    // so it might make sense to inject into DefaultSuiteNotifier a class which
    // adds the timestamps and forwards the events to an timestampful interface.
}
