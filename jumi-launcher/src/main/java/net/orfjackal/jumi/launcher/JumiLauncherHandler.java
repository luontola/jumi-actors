// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.launcher;

import net.orfjackal.jumi.core.SuiteStateCollector;
import net.orfjackal.jumi.core.events.*;
import org.jboss.netty.channel.*;

public class JumiLauncherHandler extends SimpleChannelHandler {

    // TODO: this collector belongs somewhere else, because the network connection's lifetime is longer
    private final SuiteStateCollector suite;

    public JumiLauncherHandler(SuiteStateCollector suite) {
        this.suite = suite;
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        e.getChannel().write("RunTests");
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof SuiteStartedEvent) {
            suite.onSuiteStarted();
        }
        if (e.getMessage() instanceof SuiteFinishedEvent) {
            suite.onSuiteFinished();
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}
