// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.ComposedEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.*;
import fi.jumi.core.config.Configuration;
import fi.jumi.core.events.*;
import fi.jumi.core.network.*;
import fi.jumi.core.util.PrefixedThreadFactory;
import fi.jumi.daemon.timeout.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;
import java.util.concurrent.*;

@ThreadSafe
public class Main {

    public static void main(String[] args) {
        System.out.println("Jumi Daemon starting up..."); // TODO: write a test for this and also print version number

        Configuration config = Configuration.parse(args, System.getProperties());

        // timeouts for shutting down this daemon process
        Timeout startupTimeout = new CommandExecutingTimeout(
                new SystemExit("timed out before anybody connected"), config.startupTimeout, TimeUnit.MILLISECONDS
        );
        startupTimeout.start();
        Timeout idleTimeout = new CommandExecutingTimeout(
                new SystemExit("timed out after everybody disconnected"), config.idleTimeout, TimeUnit.MILLISECONDS
        );

        // logging configuration
        PrintStream logOutput = System.out;
        FailureHandler failureHandler = new PrintStreamFailureLogger(logOutput);
        MessageListener messageListener = config.logActorMessages
                ? new PrintStreamMessageLogger(logOutput)
                : new NullMessageListener();

        // thread pool configuration
        Executor actorsThreadPool = // messages already logged by the Actors implementation
                Executors.newCachedThreadPool(new PrefixedThreadFactory("jumi-actors-"));
        // TODO: do not create unlimited numbers of threads; make it by default CPUs+1 or something
        Executor testsThreadPool = messageListener.getListenedExecutor(
                Executors.newCachedThreadPool(new PrefixedThreadFactory("jumi-tests-")));

        // actors configuration
        // TODO: not all of these eventizers might be needed - create a statistics gathering EventizerProvider
        MultiThreadedActors actors = new MultiThreadedActors(
                actorsThreadPool,
                new ComposedEventizerProvider(
                        new StartableEventizer(),
                        new RunnableEventizer(),
                        new WorkerListenerEventizer(),
                        new TestClassFinderListenerEventizer(),
                        new SuiteListenerEventizer(),
                        new CommandListenerEventizer(),
                        new TestClassListenerEventizer()
                ),
                failureHandler,
                messageListener
        );

        // bootstrap the system
        ActorThread actorThread = actors.startActorThread();
        ActorRef<CommandListener> coordinator =
                actorThread.bindActor(CommandListener.class, new TestRunCoordinator(actorThread, testsThreadPool));

        NetworkClient client = new NettyNetworkClient();
        client.connect("127.0.0.1", config.launcherPort, new DaemonNetworkEndpoint(coordinator, startupTimeout, idleTimeout));
    }
}
