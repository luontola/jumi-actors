// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;
import fj.*;
import fj.data.TreeMap;

import javax.annotation.concurrent.Immutable;

@Immutable
public class SuiteResults {

    private static final F<TestResults, Boolean> IS_PASSING = new F<TestResults, Boolean>() {
        public Boolean f(TestResults tr) {
            return tr.failure == null;
        }
    };

    // TODO: save a stream of events instead of building a model?
    private final boolean finished;
    private final TreeMap<TestId, TestResults> tests;

    public SuiteResults() {
        this(false, TreeMap.<TestId, TestResults>empty(Ord.<TestId>comparableOrd()));
    }

    public SuiteResults(boolean finished, TreeMap<TestId, TestResults> tests) {
        this.finished = finished;
        this.tests = tests;
    }

    public boolean isFinished() {
        return finished;
    }

    public SuiteResults withFinished(boolean finished) {
        return new SuiteResults(finished, tests);
    }

    public SuiteResults withTest(String testClass, TestId id, String name) {
        return new SuiteResults(finished, tests.set(id, new TestResults(name)));
    }

    public SuiteResults withFailure(String testClass, TestId id, Throwable cause) {
        TestResults prev = tests.get(id).some();
        TestResults next = prev.withFailure(cause);
        return new SuiteResults(finished, tests.set(id, next));
    }

    public int getTotalTests() {
        return tests.size();
    }

    public int getPassingTests() {
        return tests.values().filter(IS_PASSING).length();
    }

    public int getFailingTests() {
        return tests.values().removeAll(IS_PASSING).length();
    }

    @Immutable
    private static class TestResults {
        public final String name;
        public final Throwable failure;

        public TestResults(String name) {
            this(name, null);
        }

        private TestResults(String name, Throwable failure) {
            this.name = name;
            this.failure = failure;
        }

        public TestResults withFailure(Throwable cause) {
            assert this.failure == null;
            return new TestResults(name, cause);
        }
    }
}
