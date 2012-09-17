// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.testbench;

import fi.jumi.api.drivers.*;
import fi.jumi.core.results.*;
import fi.jumi.core.runs.RunId;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

public class TestBenchTest {

    private final TestBench testBench = new TestBench();

    @Test
    public void gives_events_about_what_tests_print() {
        RunId run1 = new RunId(1);
        Class<DummyTest> testClass = DummyTest.class;
        RunVisitor visitor = mock(RunVisitor.class);

        testBench.setDriverFinder(new StubDriverFinder(new Driver() {
            @Override
            public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
                notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
                TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);

                testBench.out.println("printed to out");
                testBench.err.println("printed to err");

                tn.fireTestFinished();
            }
        }));
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitRun(run1, visitor);

        verify(visitor).onPrintedOut(run1, testClass.getName(), TestId.ROOT, "printed to out");
        verify(visitor).onPrintedErr(run1, testClass.getName(), TestId.ROOT, "printed to err");
    }

    private static class DummyTest {
    }
}
