// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.runs.RunId;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SuiteRunnerTest extends SuiteRunnerIntegrationHelper {

    private static final Class<?> CLASS_1 = DummyTest.class;
    private static final Class<?> CLASS_2 = SecondDummyTest.class;

    @Test
    public void runs_all_test_classes_which_are_found() {
        Driver anyDriver = mock(Driver.class);

        run(anyDriver, CLASS_1, CLASS_2);

        assertRunsTestClass(CLASS_1, anyDriver);
        assertRunsTestClass(CLASS_2, anyDriver);
    }

    @Test
    public void runs_each_test_class_using_its_own_driver() {
        Driver driverForClass1 = mock(Driver.class, "driver1");
        Driver driverForClass2 = mock(Driver.class, "driver2");

        run(new FakeDriverFinder()
                .map(CLASS_1, driverForClass1)
                .map(CLASS_2, driverForClass2), CLASS_1, CLASS_2);

        assertRunsTestClass(CLASS_1, driverForClass1);
        assertRunsTestClass(CLASS_2, driverForClass2);
    }

    private static void assertRunsTestClass(Class<?> testClass, Driver driverForAllClasses) {
        verify(driverForAllClasses).findTests(eq(testClass), any(SuiteNotifier.class), any(Executor.class));
    }

    /**
     * Responsibility delegated to {@link fi.jumi.core.runners.DuplicateOnTestFoundEventFilter}
     */
    @Test
    public void filters_duplicate_onTestFound_events() {
        expect.onSuiteStarted();
        expect.onTestFound(CLASS_1.getName(), TestId.ROOT, "fireTestFound called twice");
        expect.onSuiteFinished();

        runAndCheckExpectations(new DuplicateFireTestFoundDriver(), CLASS_1);
    }

    @Test
    public void notifies_when_all_test_classes_are_finished() {
        // TODO: these expectations are not interesting for this test - find a way to write this test without mentioning them
        expect.onSuiteStarted();

        expect.onTestFound(CLASS_1.getName(), TestId.ROOT, "DummyTest");
        expect.onRunStarted(new RunId(1), CLASS_1.getName());
        expect.onTestStarted(new RunId(1), CLASS_1.getName(), TestId.ROOT);
        expect.onTestFinished(new RunId(1), CLASS_1.getName(), TestId.ROOT);
        expect.onRunFinished(new RunId(1));

        expect.onTestFound(CLASS_2.getName(), TestId.ROOT, "SecondDummyTest");
        expect.onRunStarted(new RunId(2), CLASS_2.getName());
        expect.onTestStarted(new RunId(2), CLASS_2.getName(), TestId.ROOT);
        expect.onTestFinished(new RunId(2), CLASS_2.getName(), TestId.ROOT);
        expect.onRunFinished(new RunId(2));

        // this must happen last, once
        expect.onSuiteFinished();

        runAndCheckExpectations(new FakeTestClassDriver(), CLASS_1, CLASS_2);
    }


    // guinea pigs

    private static class DummyTest {
    }

    private static class SecondDummyTest {
    }

    public static class DuplicateFireTestFoundDriver implements Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "fireTestFound called twice");
            notifier.fireTestFound(TestId.ROOT, "fireTestFound called twice");
        }
    }

    public static class FakeTestClassDriver implements Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            notifier.fireTestStarted(TestId.ROOT)
                    .fireTestFinished();
        }
    }

    private static class FakeDriverFinder implements DriverFinder {
        private final Map<Class<?>, Driver> driverMapping = new HashMap<Class<?>, Driver>();

        @Override
        public Driver findTestClassDriver(Class<?> testClass) {
            Driver driver = driverMapping.get(testClass);
            if (driver == null) {
                throw new IllegalArgumentException("unexpected class: " + testClass);
            }
            return driver;
        }

        public FakeDriverFinder map(Class<?> testClass, Driver driver) {
            driverMapping.put(testClass, driver);
            return this;
        }
    }
}
