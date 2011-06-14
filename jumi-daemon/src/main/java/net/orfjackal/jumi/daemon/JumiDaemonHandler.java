// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.daemon;

import net.orfjackal.jumi.core.events.*;
import org.jboss.netty.channel.*;

public class JumiDaemonHandler extends SimpleChannelHandler {

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        String command = (String) e.getMessage();
        if (command.equals("RunTests")) {
            e.getChannel().write(new SuiteStartedEvent());
            e.getChannel().write(new SuiteFinishedEvent());
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
