// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.Event;
import fi.jumi.actors.mq.MessageSender;
import fi.jumi.core.*;
import org.jboss.netty.channel.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class JumiLauncherHandler extends SimpleChannelHandler {

    private final MessageSender<Event<SuiteListener>> target;
    private volatile Event<CommandListener> startupCommand;

    public JumiLauncherHandler(MessageSender<Event<SuiteListener>> target) {
        this.target = target;
    }

    public void setStartupCommand(Event<CommandListener> startupCommand) {
        // XXX: do not pass this command like this, but use events
        this.startupCommand = startupCommand;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // TODO: move the responsibility of sending this command into JumiLauncher (requires actor model?)
        // TODO: send an event that the we have connected?
        e.getChannel().write(startupCommand);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        target.send((Event<SuiteListener>) e.getMessage());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
