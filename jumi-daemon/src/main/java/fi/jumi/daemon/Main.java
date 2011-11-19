// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.MultiThreadedActors;
import fi.jumi.core.*;
import fi.jumi.core.events.command.CommandListenerFactory;
import fi.jumi.core.events.runnable.RunnableFactory;
import fi.jumi.core.events.startable.StartableFactory;
import fi.jumi.core.events.suite.SuiteListenerFactory;
import fi.jumi.core.events.testclass.TestClassListenerFactory;
import fi.jumi.core.events.testclassfinder.TestClassFinderListenerFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;

import javax.annotation.concurrent.ThreadSafe;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

@ThreadSafe
public class Main {

    public static void main(String[] args) {
        exitWhenNotAnymoreInUse();

        int launcherPort = Integer.parseInt(args[0]);

        MultiThreadedActors actors = new MultiThreadedActors(
                new StartableFactory(),
                new RunnableFactory(),
                new TestClassFinderListenerFactory(),
                new SuiteListenerFactory(),
                new CommandListenerFactory(),
                new TestClassListenerFactory()
        );
        // TODO: support an asynchronous thread pool - the SuiteRunner must wait until the pool is idle
        Executor executor = new Executor() {
            public void execute(Runnable command) {
                command.run();
            }
        };
        CommandListener toCoordinator = actors.createPrimaryActor(CommandListener.class, new TestRunCoordinator(actors, executor), "Coordinator");

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
