// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.TestId;

public interface SuiteListener {

    // TODO: code-generate all the event classes based on this interface

    void onSuiteStarted();

    void onSuiteFinished();

    // XXX: these two events are internal to daemon; they shouldn't be part of this interface?
    void onTestClassStarted(Class<?> testClass);

    void onTestClassFinished(Class<?> testClass);

    // TODO: use Class parameters internally? have a different interface for remote use?
    void onTestFound(String testClass, TestId id, String name);

    // FIXME: this interfaces is used for events sent from daemon to launcher; it shouldn't contain any Class instances as parameters
}
