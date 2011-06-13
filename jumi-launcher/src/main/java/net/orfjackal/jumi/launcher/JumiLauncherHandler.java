// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.launcher;

import org.jboss.netty.channel.*;

public class JumiLauncherHandler extends SimpleChannelHandler {
    private volatile int totalTestsRun = -1;
    private volatile boolean testsFinished = false;

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        e.getChannel().write("RunTests");
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        String message = (String) e.getMessage();
        if (message.startsWith("TotalTestsRun:")) {
            totalTestsRun = Integer.parseInt(message.split(":")[1]);
            testsFinished = true;
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // TODO: better error handling
        e.getCause().printStackTrace();
    }

    public int getTotalTestsRun() {
        return totalTestsRun;
    }

    public boolean isTestsFinished() {
        return testsFinished;
    }
}
