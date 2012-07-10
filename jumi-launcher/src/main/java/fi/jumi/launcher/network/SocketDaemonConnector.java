// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.network;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.*;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.launcher.JumiLauncher;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;

import javax.annotation.concurrent.*;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

@Immutable
public class SocketDaemonConnector implements DaemonConnector {

    @Override
    public int listenForDaemonConnection(MessageSender<Event<SuiteListener>> eventTarget, List<File> classPath, String testsToIncludePattern) {
        // TODO: extract this thing into an actor
        // XXX: send startup command properly, using a message queue
        Event<CommandListener> startupCommand = generateStartupCommand(classPath, testsToIncludePattern);
        final DaemonConnectorHandler handler = new DaemonConnectorHandler(eventTarget, startupCommand);

        ChannelFactory factory =
                new OioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        @ThreadSafe
        class MyChannelPipelineFactory implements ChannelPipelineFactory {
            @Override
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.softCachingResolver(JumiLauncher.class.getClassLoader())),
                        handler);
            }
        }
        bootstrap.setPipelineFactory(new MyChannelPipelineFactory());

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        Channel ch = bootstrap.bind(new InetSocketAddress(0));
        InetSocketAddress addr = (InetSocketAddress) ch.getLocalAddress();
        return addr.getPort();
    }

    private Event<CommandListener> generateStartupCommand(List<File> classPath, String testsToIncludePattern) {
        MessageQueue<Event<CommandListener>> spy = new MessageQueue<Event<CommandListener>>();
        new CommandListenerEventizer().newFrontend(spy).runTests(classPath, testsToIncludePattern);
        return spy.poll();
    }


    @ThreadSafe
    public static class DaemonConnectorHandler extends SimpleChannelHandler {

        private final MessageSender<Event<SuiteListener>> target;
        private final Event<CommandListener> startupCommand;

        public DaemonConnectorHandler(MessageSender<Event<SuiteListener>> target, Event<CommandListener> startupCommand) {
            this.target = target;
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
}
