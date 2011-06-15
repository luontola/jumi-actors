// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.launcher;

import net.orfjackal.jumi.core.actors.MessageSender;
import net.orfjackal.jumi.core.commands.RunTestsCommand;
import net.orfjackal.jumi.core.events.SuiteEvent;
import org.jboss.netty.channel.*;

public class JumiLauncherHandler extends SimpleChannelHandler {

    private final MessageSender<SuiteEvent> suiteListener;

    public JumiLauncherHandler(MessageSender<SuiteEvent> suiteListener) {
        this.suiteListener = suiteListener;
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        e.getChannel().write(new RunTestsCommand());
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        suiteListener.send((SuiteEvent) e.getMessage());
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
