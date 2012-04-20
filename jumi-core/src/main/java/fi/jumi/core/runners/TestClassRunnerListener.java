// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

public interface TestClassRunnerListener extends TestClassListener {

    // TODO: we might be able to get rid of this class by renewing worker counting
    // The purpose for the existence of this interface right now is to
    // help SuiteRunner to implement the onSuiteFinished event.

    void onTestClassFinished();
}
