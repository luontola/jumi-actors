// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.api.drivers.TestId;

import javax.annotation.concurrent.Immutable;

@Immutable
public class GlobalTestId {

    private final String testClass;
    private final TestId id;

    public GlobalTestId(String testClass, TestId id) {
        this.testClass = testClass;
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        GlobalTestId that = (GlobalTestId) other;
        return this.testClass.equals(that.testClass) &&
                this.id.equals(that.id);

    }

    @Override
    public int hashCode() {
        int result = testClass.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
