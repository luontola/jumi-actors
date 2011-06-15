// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.events;

import net.orfjackal.jumi.core.SuiteListener;

import java.io.Serializable;

public class SuiteStartedEvent implements SuiteEvent, Serializable {
    public void fireOn(SuiteListener target) {
        target.onSuiteStarted();
    }
}
