// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.commands;

import net.orfjackal.jumi.core.CommandListener;
import net.orfjackal.jumi.core.actors.MessageSender;

public class CommandReceiver implements MessageSender<Command> {
    private final CommandListener listener;

    public CommandReceiver(CommandListener listener) {
        this.listener = listener;
    }

    public void send(Command message) {
        message.fireOn(listener);
    }
}
