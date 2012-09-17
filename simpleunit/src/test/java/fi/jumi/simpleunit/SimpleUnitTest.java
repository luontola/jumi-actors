// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.simpleunit;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.results.*;
import fi.jumi.core.runs.RunId;
import fi.jumi.core.testbench.TestBench;
import fi.jumi.core.util.SpyListener;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SimpleUnitTest {

    private static final RunId RUN_1 = new RunId(1);

    private final TestBench testBench = new TestBench();

    // TODO: think of a high-level API to write tests against, so that it hides Jumi's low-level event protocol

    @Test
    public void the_test_class_is_named_after_its_simple_name() {
        Class<OnePassingTest> testClass = OnePassingTest.class;

        SuiteEventDemuxer results = testBench.run(testClass);

        assertThat(results.getTestName(testClass.getName(), TestId.ROOT), is("OnePassingTest"));
    }

    @Test
    public void the_tests_are_methods_whose_name_starts_with_test() throws InterruptedException {
        Class<OnePassingTest> testClass = OnePassingTest.class;

        SuiteEventDemuxer results = testBench.run(testClass);

        assertThat(results.getTestName(testClass.getName(), TestId.of(0)), is("testPassing"));
    }

    @Test
    public void reports_test_execution() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<OnePassingTest> testClass = OnePassingTest.class;
        listener.onRunStarted(RUN_1, testClass.getName());
        listener.onTestStarted(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onTestStarted(RUN_1, testClass.getName(), TestId.of(0));
        listener.onTestFinished(RUN_1, testClass.getName(), TestId.of(0));
        listener.onTestFinished(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onRunFinished(RUN_1, testClass.getName());

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();
    }

    @Test
    public void reports_test_failure() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<OneFailingTest> testClass = OneFailingTest.class;
        listener.onRunStarted(RUN_1, testClass.getName());
        listener.onTestStarted(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onTestStarted(RUN_1, testClass.getName(), TestId.of(0));
        listener.onFailure(RUN_1, testClass.getName(), TestId.of(0), new AssertionError("dummy failure"));
        listener.onTestFinished(RUN_1, testClass.getName(), TestId.of(0));
        listener.onTestFinished(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onRunFinished(RUN_1, testClass.getName());

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();
    }

    @Test
    public void reports_failures_in_constructor() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<FailureInConstructorTest> testClass = FailureInConstructorTest.class;
        listener.onRunStarted(RUN_1, testClass.getName());
        listener.onTestStarted(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onFailure(RUN_1, testClass.getName(), TestId.ROOT, new RuntimeException("dummy exception"));
        listener.onTestFinished(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onRunFinished(RUN_1, testClass.getName());

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();

        assertThat("should find the test method even though it fails to run it",
                results.getTestName(testClass.getName(), TestId.of(0)), is("testNotExecuted"));
    }

    @Test
    public void reports_illegal_test_method_signatures() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<IllegalTestMethodSignatureTest> testClass = IllegalTestMethodSignatureTest.class;
        listener.onRunStarted(RUN_1, testClass.getName());
        listener.onTestStarted(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onTestStarted(RUN_1, testClass.getName(), TestId.of(0));
        listener.onFailure(RUN_1, testClass.getName(), TestId.of(0), new IllegalArgumentException("wrong number of arguments"));
        listener.onTestFinished(RUN_1, testClass.getName(), TestId.of(0));
        listener.onTestFinished(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onRunFinished(RUN_1, testClass.getName());

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();

        assertThat("should find the test method even though it fails to run it",
                results.getTestName(testClass.getName(), TestId.of(0)), is("testMethodWithParameters"));
    }

    @Test
    public void reports_an_error_if_the_test_class_contains_no_test_methods() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<NoTestMethodsTest> testClass = NoTestMethodsTest.class;
        listener.onRunStarted(RUN_1, testClass.getName());
        listener.onTestStarted(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onFailure(RUN_1, testClass.getName(), TestId.ROOT,
                new IllegalArgumentException("No test methods in class fi.jumi.simpleunit.NoTestMethodsTest"));
        listener.onTestFinished(RUN_1, testClass.getName(), TestId.ROOT);
        listener.onRunFinished(RUN_1, testClass.getName());

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();
    }
}
