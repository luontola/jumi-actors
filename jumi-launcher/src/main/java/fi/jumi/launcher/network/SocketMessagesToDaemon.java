// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.network;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.CommandListener;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.launcher.SuiteOptions;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.util.List;

@ThreadSafe
public class SocketMessagesToDaemon implements MessagesToDaemon {

    private final MessageSender<Event<CommandListener>> sender;

    public SocketMessagesToDaemon(MessageSender<Event<CommandListener>> sender) {
        this.sender = sender;
    }

    @Override
    public void runTests(SuiteOptions suiteOptions) {
        sender.send(generateStartupCommand(suiteOptions.classPath, suiteOptions.testsToIncludePattern));
    }

    private static Event<CommandListener> generateStartupCommand(List<File> classPath, String testsToIncludePattern) {
        MessageQueue<Event<CommandListener>> spy = new MessageQueue<Event<CommandListener>>();
        new CommandListenerEventizer().newFrontend(spy).runTests(classPath, testsToIncludePattern);
        return spy.poll();
    }
}
