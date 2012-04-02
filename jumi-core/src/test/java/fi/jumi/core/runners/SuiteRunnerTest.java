// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.*;
import org.junit.Test;

import java.util.concurrent.Executor;

public class SuiteRunnerTest extends SuiteRunnerIntegrationHelper {

    // TODO: launches every testclass found, using its driver

    /**
     * Responsibility delegated to {@link fi.jumi.core.runners.DuplicateOnTestFoundEventFilter}
     */
    @Test
    public void filters_duplicate_onTestFound_events() {
        expect.onSuiteStarted();
        expect.onTestFound(DummyTest.class.getName(), TestId.ROOT, "fireTestFound called twice");
        expect.onSuiteFinished();

        runAndCheckExpectations(DuplicateFireTestFoundDriver.class, DummyTest.class);
    }

    // TODO: notifies when all testclasses are finished


    // guinea pigs

    private static class DummyTest {
    }

    public static class DuplicateFireTestFoundDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, "fireTestFound called twice");
            notifier.fireTestFound(TestId.ROOT, "fireTestFound called twice");
        }
    }
}
