// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.events;

import net.orfjackal.jumi.api.drivers.TestId;
import net.orfjackal.jumi.core.SuiteListener;
import net.orfjackal.jumi.core.actors.*;

public class SuiteListenerToSuiteEvent implements SuiteListener {

    private final MessageSender<Event<SuiteListener>> target;

    public SuiteListenerToSuiteEvent(MessageSender<Event<SuiteListener>> target) {
        this.target = target;
    }

    public void onSuiteStarted() {
        target.send(new SuiteStartedEvent());
    }

    public void onSuiteFinished() {
        target.send(new SuiteFinishedEvent());
    }

    public void onTestFound(TestId id, String name) {
        target.send(new TestFoundEvent(id, name));
    }
}
