// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.TestId;

public interface SuiteListener {

    // TODO: code-generate all the event classes based on this interface

    void onSuiteStarted();

    void onSuiteFinished();

    // TODO: mention also the test class?
    void onTestFound(TestId id, String name);
}
