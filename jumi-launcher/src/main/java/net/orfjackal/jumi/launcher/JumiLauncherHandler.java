// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.launcher;

import net.orfjackal.jumi.core.actors.MessageSender;
import net.orfjackal.jumi.core.commands.RunTestsCommand;
import net.orfjackal.jumi.core.events.SuiteEvent;
import org.jboss.netty.channel.*;

public class JumiLauncherHandler extends SimpleChannelHandler {

    private final MessageSender<SuiteEvent> toLauncher;

    public JumiLauncherHandler(MessageSender<SuiteEvent> toLauncher) {
        this.toLauncher = toLauncher;
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // TODO: move the responsibility of sending this command into JumiLauncher
        // TODO: send an event that the we have connected?
        e.getChannel().write(new RunTestsCommand());
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        toLauncher.send((SuiteEvent) e.getMessage());
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
