// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.commands;

import net.orfjackal.jumi.core.CommandListener;
import net.orfjackal.jumi.core.actors.*;

public class CommandListenerToCommandEvent implements MessageSender<Event<CommandListener>> {
    
    private final CommandListener target;

    public CommandListenerToCommandEvent(CommandListener target) {
        this.target = target;
    }

    public void send(Event<CommandListener> message) {
        message.fireOn(target);
    }
}
