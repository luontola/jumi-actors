// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.TestId;

public interface SuiteListener {

    // NOTE: this interfaces is used for events sent from daemon to launcher; it shouldn't contain any Class instances as parameters
    // TODO: write a test which ensures the absence of Class parameters

    // TODO: code-generate all the event classes based on this interface

    void onSuiteStarted();

    void onSuiteFinished();

    void onTestFound(String testClass, TestId id, String name);
}
