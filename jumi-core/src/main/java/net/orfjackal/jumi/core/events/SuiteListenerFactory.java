// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.events;

import net.orfjackal.jumi.core.SuiteListener;
import net.orfjackal.jumi.core.actors.*;

public class SuiteListenerFactory implements ListenerFactory<SuiteListener> {

    public Class<SuiteListener> getType() {
        return SuiteListener.class;
    }

    public SuiteListener createSenderWrapper(MessageSender<Event<SuiteListener>> sender) {
        return new SuiteEventSender(sender);
    }

    public MessageSender<Event<SuiteListener>> createReceiver(SuiteListener listener) {
        return new SuiteEventReceiver(listener);
    }
}
