// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.daemon.Daemon;
import fi.jumi.launcher.network.DaemonConnector;
import fi.jumi.launcher.process.ProcessLauncher;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;

import javax.annotation.concurrent.*;
import java.io.*;
import java.util.*;

@ThreadSafe
public class JumiLauncher {

    private final MessageQueue<Event<SuiteListener>> eventQueue = new MessageQueue<Event<SuiteListener>>();
    private final DaemonConnector daemonConnector;
    private final ProcessLauncher processLauncher;

    private final File jumiHome;
    private final List<File> classPath = new ArrayList<File>();
    private final List<String> jvmOptions = new ArrayList<String>();
    private final Properties systemProperties = new Properties();

    private String testsToIncludePattern;
    private Writer outputListener = new NullWriter();

    public JumiLauncher(DaemonConnector daemonConnector, ProcessLauncher processLauncher, File jumiHome) {
        this.daemonConnector = daemonConnector;
        this.processLauncher = processLauncher;
        this.jumiHome = jumiHome; // TODO: default to "~/.jumi" (create default constructor?)
    }

    public MessageReceiver<Event<SuiteListener>> getEventStream() {
        return eventQueue;
    }

    public void start() throws IOException {
        int port = daemonConnector.listenForDaemonConnection(eventQueue, classPath, testsToIncludePattern);
        startProcess(port);
    }

    private void startProcess(int launcherPort) throws IOException {
        InputStream embeddedJar = Daemon.getDaemonJarAsStream();
        File extractedJar = new File(jumiHome, "lib/" + Daemon.getDaemonJarName());
        copyToFile(embeddedJar, extractedJar);

        Process process = processLauncher.startJavaProcess(
                extractedJar,
                jumiHome,
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

    private static void copyToFile(InputStream in, File destination) throws IOException {
        ensureDirExists(destination.getParentFile());
        OutputStream out = null;
        try {
            out = new FileOutputStream(destination);
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    private static void ensureDirExists(File dir) throws IOException {
        if (dir.isDirectory()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new IOException("Unable to create directory: " + dir);
        }
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
