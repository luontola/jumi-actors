// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.runners;

import net.orfjackal.jumi.api.drivers.TestId;

public interface TestClassRunnerListener {

    void onTestFound(TestId id, String name);

    void onTestClassFinished();
}
