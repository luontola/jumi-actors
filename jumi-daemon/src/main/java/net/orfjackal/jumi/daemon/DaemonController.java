// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.daemon;

import net.orfjackal.jumi.core.SuiteListener;
import net.orfjackal.jumi.core.actors.MessageSender;
import net.orfjackal.jumi.core.commands.*;

public class DaemonController implements MessageSender<Command> {

    // TODO: support for multiple clients?
    private SuiteListener listener = null;

    public void send(Command message) {
        if (message instanceof AddSuiteListener) {
            AddSuiteListener m = (AddSuiteListener) message;
            listener = m.getListener();
        }
        if (message instanceof RunTestsCommand) {
            // TODO: extract class which interprets commands
            // TODO: extract class which runs tests
            // TODO: extract class which notifies all listeners
            listener.onSuiteStarted();
            listener.onSuiteFinished();
        }
    }
}
