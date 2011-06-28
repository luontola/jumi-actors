// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.commands;

import net.orfjackal.jumi.core.*;
import net.orfjackal.jumi.core.actors.*;

import java.io.File;
import java.util.List;

public class CommandEventToCommandListener implements CommandListener {

    private final MessageSender<Event<CommandListener>> target;

    public CommandEventToCommandListener(MessageSender<Event<CommandListener>> target) {
        this.target = target;
    }

    public void addSuiteListener(SuiteListener listener) {
        target.send(new AddSuiteListener(listener));
    }

    public void runTests(List<File> classPath, String testsToIncludePattern) {
        target.send(new RunTestsCommand(classPath, testsToIncludePattern));
    }
}
