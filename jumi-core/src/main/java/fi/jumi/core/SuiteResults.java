// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;
import fj.Ord;
import fj.data.TreeMap;

import javax.annotation.concurrent.Immutable;

@Immutable
public class SuiteResults {

    private final boolean finished;
    private final TreeMap<TestId, String> tests;

    public SuiteResults() {
        this(false, TreeMap.<TestId, String>empty(Ord.<TestId>comparableOrd()));
    }

    public SuiteResults(boolean finished, TreeMap<TestId, String> tests) {
        this.finished = finished;
        this.tests = tests;
    }

    public boolean isFinished() {
        return finished;
    }

    public SuiteResults withFinished(boolean finished) {
        return new SuiteResults(finished, tests);
    }

    public SuiteResults withTest(TestId id, String name) {
        return new SuiteResults(finished, tests.set(id, name));
    }

    public int getTotalTests() {
        return tests.size();
    }

    public int getPassingTests() {
        return tests.size(); // TODO
    }

    public int getFailingTests() {
        return 0; // TODO
    }
}
