// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.network.NettyNetworkServer;
import fi.jumi.core.util.PrefixedThreadFactory;
import fi.jumi.launcher.daemon.*;
import fi.jumi.launcher.process.*;
import fi.jumi.launcher.remote.*;
import org.apache.commons.io.output.NullWriter;

import java.io.*;
import java.util.concurrent.Executors;

public class JumiLauncherBuilder {

    private boolean debugLogging = false;

    public JumiLauncherBuilder enableDebugLogging() {
        debugLogging = true;
        return this;
    }

    public JumiLauncher build() {
        Actors actors = new MultiThreadedActors(
                Executors.newCachedThreadPool(new PrefixedThreadFactory("jumi-launcher-")),
                new DynamicEventizerProvider(),
                new PrintStreamFailureLogger(System.out),
                new NullMessageListener()
        );
        ActorThread actorThread = createActorThread(actors);

        ActorRef<DaemonSummoner> daemonSummoner = actorThread.bindActor(DaemonSummoner.class, new ProcessStartingDaemonSummoner(
                new DirBasedSteward(new EmbeddedDaemonJar(), getSettingsDirectory()),
                createProcessStarter(),
                new NettyNetworkServer(debugLogging),
                createDaemonOutputListener()
        ));
        ActorRef<SuiteLauncher> suiteLauncher = actorThread.bindActor(SuiteLauncher.class, new RemoteSuiteLauncher(actorThread, daemonSummoner));

        return new JumiLauncher(actorThread, suiteLauncher);
    }

    protected ActorThread createActorThread(Actors actors) {
        return actors.startActorThread();
    }

    protected File getSettingsDirectory() {
        return new File(".jumi"); // TODO: put into user home
    }

    protected ProcessStarter createProcessStarter() {
        return new SystemProcessStarter();
    }

    protected Writer createDaemonOutputListener() {
        return new NullWriter();
    }
}
