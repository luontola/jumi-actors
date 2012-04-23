// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.*;
import fi.jumi.core.*;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.launcher.daemon.Daemon;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;

import javax.annotation.concurrent.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

// TODO: annotate all classes

@ThreadSafe
public class JumiLauncher {
    private File jumiHome; // TODO: default to "~/.jumi"
    private Writer outputListener = new NullWriter();
    private final File javaExecutable = new File(System.getProperty("java.home"), "bin/java");
    private Process process;

    private final JumiLauncherHandler handler;
    private final List<File> classPath = new ArrayList<File>();
    private String testsToIncludePattern;
    private String[] jvmOptions = new String[0];

    public JumiLauncher(MessageSender<Event<SuiteListener>> eventTarget) {
        handler = new JumiLauncherHandler(eventTarget);
    }

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
        // XXX: send startup command properly, using a message queue
        handler.setStartupCommand(generateStartupCommand());

        int port = listenForDaemonConnection();
        startProcess(port);
    }

    private Event<CommandListener> generateStartupCommand() {
        MessageQueue<Event<CommandListener>> spy = new MessageQueue<Event<CommandListener>>();
        new CommandListenerEventizer().newFrontend(spy).runTests(classPath, testsToIncludePattern);
        return spy.poll();
    }

    private int listenForDaemonConnection() {
        ChannelFactory factory =
                new OioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        @ThreadSafe
        class MyChannelPipelineFactory implements ChannelPipelineFactory {
            @Override
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.softCachingResolver(JumiLauncher.class.getClassLoader())),
                        handler);
            }
        }
        bootstrap.setPipelineFactory(new MyChannelPipelineFactory());

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

        List<String> command = new ArrayList<String>();
        command.add(javaExecutable.getAbsolutePath());
        Collections.addAll(command, jvmOptions);
        command.add("-jar");
        command.add(extractedJar.getAbsolutePath());
        command.add(String.valueOf(launcherPort));

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(jumiHome);
        builder.redirectErrorStream(true);
        builder.command(command);
        process = builder.start();

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

    public void addToClassPath(File file) {
        classPath.add(file);
        // TODO: support for main and test class paths
    }

    public void setTestsToInclude(String pattern) {
        testsToIncludePattern = pattern;
    }

    public void setJvmOptions(String... jvmOptions) {
        this.jvmOptions = jvmOptions;
    }
}
