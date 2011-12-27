// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.Event;
import fi.jumi.actors.MessageSender;
import fi.jumi.core.SuiteListener;

import java.util.ArrayList;
import java.util.List;

public class FakeStream implements MessageSender<Event<SuiteListener>> {
    
    private final List<Event<SuiteListener>> messages = new ArrayList<Event<SuiteListener>>();
    private int cursor = 0;

    public void send(Event<SuiteListener> message) {
        messages.add(message);
    }

    public Event<SuiteListener> poll() {
        if (cursor >= messages.size()) {
            return null;
        }
        Event<SuiteListener> message = messages.get(cursor);
        cursor++;
        return message;
    }
}
