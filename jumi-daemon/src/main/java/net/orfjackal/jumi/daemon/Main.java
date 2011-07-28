// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.daemon;

import net.orfjackal.jumi.core.*;
import net.orfjackal.jumi.core.actors.MultiThreadedActors;
import net.orfjackal.jumi.core.dynamicevents.DynamicListenerFactory;
import net.orfjackal.jumi.core.files.TestClassFinderListener;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        exitWhenNotAnymoreInUse();

        int launcherPort = Integer.parseInt(args[0]);

        MultiThreadedActors actors = new MultiThreadedActors(DynamicListenerFactory.factoriesFor(
                Startable.class,
                Runnable.class,
                TestClassFinderListener.class,
                SuiteListener.class,
                CommandListener.class
        ));
        CommandListener toCoordinator = actors.createPrimaryActor(CommandListener.class, new TestRunCoordinator(actors), "Coordinator");

        connectToLauncher(launcherPort, toCoordinator);
    }

    private static void connectToLauncher(int launcherPort, final CommandListener toCoordinator) {
        ChannelFactory factory = new OioClientSocketChannelFactory(Executors.newCachedThreadPool());
        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(),
                        new LoggingHandler(InternalLogLevel.INFO), // TODO: remove this debug code
                        new JumiDaemonHandler(toCoordinator));
            }
        });

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        bootstrap.connect(new InetSocketAddress("localhost", launcherPort));
    }

    private static void exitWhenNotAnymoreInUse() {
        // TODO: implement timeouts etc. which will automatically close down the daemon once the launcher is no more
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
