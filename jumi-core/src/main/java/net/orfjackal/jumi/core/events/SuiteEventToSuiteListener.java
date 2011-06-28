// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.events;

import net.orfjackal.jumi.core.SuiteListener;
import net.orfjackal.jumi.core.actors.*;

public class SuiteEventToSuiteListener implements MessageSender<Event<SuiteListener>> {

    private final SuiteListener target;

    public SuiteEventToSuiteListener(SuiteListener target) {
        this.target = target;
    }

    public void send(Event<SuiteListener> message) {
        message.fireOn(target);
    }
}
