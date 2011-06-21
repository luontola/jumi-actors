// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.launcher;

import net.orfjackal.jumi.core.SuiteStateCollector;
import net.orfjackal.jumi.core.commands.RunTestsCommand;
import net.orfjackal.jumi.core.events.SuiteEventReceiver;
import net.orfjackal.jumi.launcher.daemon.Daemon;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

public class JumiLauncher {
    private File jumiHome; // TODO: default to "~/.jumi"
    private Writer outputListener = new NullWriter();
    private File javaExecutable = new File(System.getProperty("java.home"), "bin/java");
    private Process process;

    private final SuiteStateCollector suite = new SuiteStateCollector();
    private final JumiLauncherHandler handler = new JumiLauncherHandler(new SuiteEventReceiver(suite));
    private final List<File> classPath = new ArrayList<File>();
    private String testsToIncludePattern;

    // TODO: this class has multiple responsibilities, split to smaller parts?
    // - configuring the test run
    // - starting up the daemon process
    // - connecting to the daemon over a socket
    // - sending commands to the daemon

    public void setJumiHome(File jumiHome) {
        this.jumiHome = jumiHome;
    }

    public void setOutputListener(Writer outputListener) {
        this.outputListener = outputListener;
    }

    public void start() throws IOException {
        handler.setStartupCommand(new RunTestsCommand(classPath, testsToIncludePattern)); // XXX: send properly using a message queue
        int port = listenForDaemonConnection();
        startProcess(port);
    }

    private int listenForDaemonConnection() {
        ChannelFactory factory =
                new OioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(),
                        handler);
            }
        });

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        Channel ch = bootstrap.bind(new InetSocketAddress(0));
        InetSocketAddress addr = (InetSocketAddress) ch.getLocalAddress();
        return addr.getPort();
    }

    private void startProcess(int launcherPort) throws IOException {
        InputStream embeddedJar = Daemon.getDaemonJarAsStream();
        File extractedJar = new File(jumiHome, "lib/" + Daemon.getDaemonJarName());
        copyToFile(embeddedJar, extractedJar);

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(jumiHome);
        builder.redirectErrorStream(true);
        builder.command(javaExecutable.getAbsolutePath(), "-jar", extractedJar.getAbsolutePath(),
                String.valueOf(launcherPort));
        process = builder.start();

        copyInBackground(process.getInputStream(), outputListener);
    }

    private void copyInBackground(final InputStream src, final Writer dest) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    IOUtils.copy(src, dest);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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

    public void awaitSuiteFinished() throws InterruptedException {
        suite.awaitSuiteFinished();
    }

    public boolean awaitSuiteFinished(long timeout, TimeUnit unit) throws InterruptedException {
        return suite.awaitSuiteFinished(timeout, unit);
    }

    public void addToClassPath(File file) {
        classPath.add(file);
        // TODO: support for main and test class paths
    }

    public void setTestsToInclude(String pattern) {
        testsToIncludePattern = pattern;
    }

    public int getTotalTests() {
        return suite.getState().getTotalTests();
    }
}
