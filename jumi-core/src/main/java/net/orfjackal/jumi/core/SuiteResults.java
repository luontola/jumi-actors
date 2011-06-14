// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

public class SuiteResults {

    private final boolean finished;

    public SuiteResults() {
        this(false);
    }

    public SuiteResults(boolean finished) {
        this.finished = finished;
    }

    public boolean isFinished() {
        return finished;
    }

    public SuiteResults withFinished(boolean finished) {
        return new SuiteResults(finished);
    }

    public int getTotalTests() {
        return 0;
    }
}
