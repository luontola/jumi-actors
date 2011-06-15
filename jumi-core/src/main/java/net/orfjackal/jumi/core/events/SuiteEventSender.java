// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.events;

import net.orfjackal.jumi.core.SuiteListener;
import net.orfjackal.jumi.core.actors.MessageSender;

public class SuiteEventSender implements SuiteListener {
    private final MessageSender<SuiteEvent> sender;

    public SuiteEventSender(MessageSender<SuiteEvent> sender) {
        this.sender = sender;
    }

    public void onSuiteStarted() {
        sender.send(new SuiteStartedEvent());
    }

    public void onSuiteFinished() {
        sender.send(new SuiteFinishedEvent());
    }
}
