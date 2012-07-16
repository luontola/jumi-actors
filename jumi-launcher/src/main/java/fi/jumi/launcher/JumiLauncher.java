// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.*;
import fi.jumi.core.config.Configuration;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.launcher.daemon.HomeManager;
import fi.jumi.launcher.network.*;
import fi.jumi.launcher.process.ProcessStarter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
import org.jboss.netty.channel.Channel;

import javax.annotation.concurrent.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

@ThreadSafe
public class JumiLauncher {

    private final MessageQueue<Event<SuiteListener>> eventQueue = new MessageQueue<Event<SuiteListener>>();
    private final HomeManager homeManager;
    private final DaemonConnector daemonConnector;
    private final ProcessStarter processStarter;

    private final List<File> classPath = new ArrayList<File>();
    private final List<String> jvmOptions = new ArrayList<String>();
    private final Properties systemProperties = new Properties();

    private String testsToIncludePattern;
    private Writer outputListener = new NullWriter();

    public JumiLauncher(HomeManager homeManager, DaemonConnector daemonConnector, ProcessStarter processStarter) {
        this.homeManager = homeManager;
        this.daemonConnector = daemonConnector;
        this.processStarter = processStarter;
    }

    public MessageReceiver<Event<SuiteListener>> getEventStream() {
        return eventQueue;
    }

    public void start() throws IOException, ExecutionException, InterruptedException {
        FutureValue<Channel> daemonConnection = new FutureValue<Channel>();
        int port = daemonConnector.listenForDaemonConnection(eventQueue, daemonConnection);
        startDaemonProcess(port);
        sendRunTestsCommand(daemonConnection);
    }

    private void startDaemonProcess(int launcherPort) throws IOException {
        Process process = processStarter.startJavaProcess(
                homeManager.getDaemonJar(),
                homeManager.getSettingsDir(),
                jvmOptions,
                systemProperties,
                Configuration.LAUNCHER_PORT, String.valueOf(launcherPort)
        );

        // TODO: write the output to a log file using OS pipes, read it from there with AppRunner
        copyInBackground(process.getInputStream(), outputListener);
    }

    private void copyInBackground(final InputStream src, final Writer dest) {
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

    private void sendRunTestsCommand(FutureValue<Channel> daemonConnection) throws InterruptedException, ExecutionException {
        // XXX: send startup command properly, using actors?
        Channel daemonChannel = daemonConnection.get();
        daemonChannel.write(generateStartupCommand(classPath, testsToIncludePattern));
    }

    private static Event<CommandListener> generateStartupCommand(List<File> classPath, String testsToIncludePattern) {
        MessageQueue<Event<CommandListener>> spy = new MessageQueue<Event<CommandListener>>();
        new CommandListenerEventizer().newFrontend(spy).runTests(classPath, testsToIncludePattern);
        return spy.poll();
    }

    public void shutdown() {
        // TODO
    }

    public void setOutputListener(Writer outputListener) {
        this.outputListener = outputListener;
    }

    public void addToClassPath(File file) {
        classPath.add(file);
        // TODO: support for main and test class paths
    }

    public void setTestsToInclude(String pattern) {
        testsToIncludePattern = pattern;
    }

    public void addJvmOptions(String... jvmOptions) {
        this.jvmOptions.addAll(Arrays.asList(jvmOptions));
    }

    public void enableMessageLogging() {
        systemProperties.setProperty(Configuration.LOG_ACTOR_MESSAGES, "true");
    }
}
