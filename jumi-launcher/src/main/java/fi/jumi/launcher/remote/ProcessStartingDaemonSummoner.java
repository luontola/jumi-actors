// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.config.Configuration;
import fi.jumi.core.network.NetworkServer;
import fi.jumi.launcher.SuiteOptions;
import fi.jumi.launcher.daemon.Steward;
import fi.jumi.launcher.process.*;
import org.apache.commons.io.IOUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;

@NotThreadSafe
public class ProcessStartingDaemonSummoner implements DaemonSummoner {

    private final Steward steward;
    private final ProcessStarter processStarter;
    private final NetworkServer daemonConnector;

    private final Writer outputListener; // TODO: remove me

    public ProcessStartingDaemonSummoner(Steward steward,
                                         ProcessStarter processStarter,
                                         NetworkServer daemonConnector,
                                         Writer outputListener) {
        this.steward = steward;
        this.processStarter = processStarter;
        this.daemonConnector = daemonConnector;
        this.outputListener = outputListener;
    }

    @Override
    public void connectToDaemon(SuiteOptions suiteOptions, ActorRef<DaemonListener> listener) {
        int port = daemonConnector.listenOnAnyPort(listener.tell());

        try {
            JvmArgs jvmArgs = new JvmArgsBuilder()
                    .executableJar(steward.getDaemonJar())
                    .workingDir(new File(".")) // TODO: get the working directory from suite options
                    .jvmOptions(suiteOptions.jvmOptions)
                    .systemProperties(suiteOptions.systemProperties)
                    .programArgs(Configuration.LAUNCHER_PORT, String.valueOf(port))
                    .toJvmArgs();
            Process process = processStarter.startJavaProcess(jvmArgs);
            copyInBackground(process.getInputStream(), outputListener); // TODO: write the output to a log file using OS pipes, read it from there with AppRunner
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
                } finally {
                    IOUtils.closeQuietly(src);
                    IOUtils.closeQuietly(dest);
                }
            }
        }
        Thread t = new Thread(new Copier(), "Daemon Output Copier");
        t.setDaemon(true);
        t.start();
    }
}
