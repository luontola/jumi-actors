// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.network;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.*;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.launcher.SuiteOptions;
import org.jboss.netty.channel.Channel;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.util.List;

@ThreadSafe
public class NettyDaemonConnection implements DaemonConnection {

    private final Channel channel;

    public NettyDaemonConnection(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void runTests(SuiteOptions suiteOptions, MessageSender<Event<SuiteListener>> suiteListener) {
        channel.write(generateStartupCommand(suiteOptions.classPath, suiteOptions.testsToIncludePattern));
    }

    private static Event<CommandListener> generateStartupCommand(List<File> classPath, String testsToIncludePattern) {
        MessageQueue<Event<CommandListener>> spy = new MessageQueue<Event<CommandListener>>();
        new CommandListenerEventizer().newFrontend(spy).runTests(classPath, testsToIncludePattern);
        return spy.poll();
    }
}
