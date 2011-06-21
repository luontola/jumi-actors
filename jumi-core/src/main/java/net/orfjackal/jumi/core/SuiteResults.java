// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.TestId;

import java.util.*;

public class SuiteResults {

    private final boolean finished;
    private final Map<TestId, String> tests;

    public SuiteResults() {
        this(false, new HashMap<TestId, String>());
    }

    public SuiteResults(boolean finished, Map<TestId, String> tests) {
        this.finished = finished;
        this.tests = new HashMap<TestId, String>(tests); // TODO: use functional collections
    }

    public boolean isFinished() {
        return finished;
    }

    public SuiteResults withFinished(boolean finished) {
        return new SuiteResults(finished, tests);
    }

    public SuiteResults withTest(TestId id, String name) {
        Map<TestId, String> tests = new HashMap<TestId, String>(this.tests);
        tests.put(id, name);
        return new SuiteResults(finished, tests);
    }

    public int getTotalTests() {
        return tests.size();
    }
}
