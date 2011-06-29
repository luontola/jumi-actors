// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.launcher;

import net.orfjackal.jumi.core.*;
import net.orfjackal.jumi.core.actors.*;
import org.jboss.netty.channel.*;

public class JumiLauncherHandler extends SimpleChannelHandler {

    private final MessageSender<Event<SuiteListener>> toLauncher;
    private volatile Event<CommandListener> startupCommand;

    public JumiLauncherHandler(MessageSender<Event<SuiteListener>> toLauncher) {
        this.toLauncher = toLauncher;
    }

    public void setStartupCommand(Event<CommandListener> startupCommand) {
        // XXX: do not pass this command like this, but use events
        this.startupCommand = startupCommand;
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // TODO: move the responsibility of sending this command into JumiLauncher (requires actor model?)
        // TODO: send an event that the we have connected?
        e.getChannel().write(startupCommand);
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        toLauncher.send((Event<SuiteListener>) e.getMessage());
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
