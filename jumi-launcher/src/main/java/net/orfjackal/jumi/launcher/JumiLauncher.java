// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.launcher;

import net.orfjackal.jumi.launcher.daemon.Daemon;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class JumiLauncher {
    private File jumiHome; // TODO: default to "~/.jumi"
    private Writer outputListener = new NullWriter();
    private File javaExecutable = new File(System.getProperty("java.home"), "bin/java");
    private Process process;

    private final JumiLauncherHandler handler = new JumiLauncherHandler();

    public JumiLauncher() {
    }

    public void setJumiHome(File jumiHome) {
        this.jumiHome = jumiHome;
    }

    public void setOutputListener(StringWriter outputListener) {
        this.outputListener = outputListener;
    }

    public void start() throws IOException {
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
        // XXX: handle the case of suite deadlocking better
        long limit = System.currentTimeMillis() + 2000;
        while (!handler.isTestsFinished() && System.currentTimeMillis() < limit) {
            Thread.sleep(1);
        }
    }

    public void addToClassPath(File file) {
        // TODO: support for main and test class paths
    }

    public void setTestsToInclude(String pattern) {
    }

    public int getTotalTests() {
        return handler.getTotalTestsRun();
    }
}
