// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;

public interface SuiteListener {

    void onSuiteStarted();

    void onSuiteFinished();

    void onTestFound(String testClass, TestId id, String name);

    void onTestStarted(String testClass, TestId id);

    void onTestFinished(String testClass, TestId id);

    void onFailure(String testClass, TestId id, Throwable cause);
}
