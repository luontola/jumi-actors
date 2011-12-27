// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.api.drivers.TestId;

public class GlobalTestId {

    private final String className;
    private final TestId testId;

    public GlobalTestId(String className, TestId testId) {
        this.className = className;
        this.testId = testId;
    }

    @Override
    public boolean equals(Object other) {
        GlobalTestId that = (GlobalTestId) other;
        return this.className.equals(that.className) &&
                this.testId.equals(that.testId);

    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + testId.hashCode();
        return result;
    }
}
