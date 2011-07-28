// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.api.drivers.TestId;

public interface SuiteListener {

    // TODO: code-generate all the event classes based on this interface

    void onSuiteStarted();

    void onSuiteFinished();

    void onTestFound(String testClass, TestId id, String name);
}
