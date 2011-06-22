// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import fj.Ord;
import fj.data.TreeMap;
import net.orfjackal.jumi.api.drivers.TestId;

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
}
