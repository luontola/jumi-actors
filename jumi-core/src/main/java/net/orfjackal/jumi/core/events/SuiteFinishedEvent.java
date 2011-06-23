// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.events;

import net.orfjackal.jumi.core.SuiteListener;
import net.orfjackal.jumi.core.actors.Event;

import java.io.Serializable;

public class SuiteFinishedEvent implements Serializable, Event<SuiteListener> {
    public void fireOn(SuiteListener target) {
        target.onSuiteFinished();
    }
}
