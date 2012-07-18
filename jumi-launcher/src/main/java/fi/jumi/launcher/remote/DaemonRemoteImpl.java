// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageQueue;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.SuiteOptions;
import fi.jumi.launcher.daemon.HomeManager;
import fi.jumi.launcher.network.*;
import fi.jumi.launcher.process.ProcessStarter;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.channel.Channel;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;

@NotThreadSafe
public class DaemonRemoteImpl implements DaemonRemote {

    private final ActorThread actorThread;
    private final HomeManager homeManager;
    private final ProcessStarter processStarter;
    private final DaemonConnector daemonConnector;

    private final MessageQueue<Event<SuiteListener>> eventQueue; // TODO: remove me
    private final Writer outputListener; // TODO: remove me

    public DaemonRemoteImpl(ActorThread actorThread,
                            HomeManager homeManager,
                            ProcessStarter processStarter,
                            DaemonConnector daemonConnector,
                            MessageQueue<Event<SuiteListener>> eventQueue,
                            Writer outputListener) {
        this.actorThread = actorThread;
        this.homeManager = homeManager;
        this.daemonConnector = daemonConnector;
        this.eventQueue = eventQueue;
        this.outputListener = outputListener;
        this.processStarter = processStarter;
    }

    @Override
    public void connectToDaemon(SuiteOptions suiteOptions, ActorRef<DaemonConnectionListener> response) {
        try {
            // XXX: this code is a mess; maybe get rid of the future?
            FutureValue<Channel> f = new FutureValue<Channel>();
            // TODO: don't pass eventQueue here, replace it with DaemonConnectionListener.onMessageFromDaemon()
            int port = daemonConnector.listenForDaemonConnection(eventQueue, f);
            Process process = processStarter.startJavaProcess(
                    homeManager.getDaemonJar(),
                    homeManager.getSettingsDir(),
                    suiteOptions.jvmOptions,
                    suiteOptions.systemProperties,
                    Configuration.LAUNCHER_PORT, String.valueOf(port)
            );
            // TODO: write the output to a log file using OS pipes, read it from there with AppRunner
            copyInBackground(process.getInputStream(), outputListener);
            Channel daemonChannel = f.get();

            // TODO: pass 'response' to listenForDaemonConnection and let it call onDaemonConnected
            response.tell().onDaemonConnected(actor(new NettyDaemonConnection(daemonChannel)));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyInBackground(final InputStream src, final Writer dest) {
        @NotThreadSafe
        class Copier implements Runnable {
            @Override
            public void run() {
                try {
                    IOUtils.copy(src, dest);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Thread t = new Thread(new Copier());
        t.setDaemon(true);
        t.start();
    }


    // actor helpers

    private ActorRef<DaemonConnection> actor(DaemonConnection rawActor) {
        return actorThread.bindActor(DaemonConnection.class, rawActor);
    }
}
