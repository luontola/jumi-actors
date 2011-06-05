// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.launcher;

import net.orfjackal.jumi.launcher.daemon.Daemon;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class JumiLauncher {
    private File jumiHome; // TODO: default to "~/.jumi"
    private Writer outputListener;
    private File javaExecutable = new File(System.getProperty("java.home"), "bin/java");
    private Process process;

    public JumiLauncher() {
    }

    public void setJumiHome(File jumiHome) {
        this.jumiHome = jumiHome;
    }

    public void setOutputListener(StringWriter outputListener) {
        this.outputListener = outputListener;
    }

    public void start() throws IOException {
        InputStream embeddedJar = Daemon.getDaemonJarAsStream();
        File extractedJar = new File(jumiHome, "lib/" + Daemon.getDaemonJarName());
        copyToFile(embeddedJar, extractedJar);

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(jumiHome);
        builder.redirectErrorStream(true);
        builder.command(javaExecutable.getAbsolutePath(), "-jar", extractedJar.getAbsolutePath());
        process = builder.start();

        Thread t = new Thread(new Runnable() {
            public void run() {
                InputStream src = process.getInputStream();
                try {
                    IOUtils.copy(src, outputListener);
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

    public void join() throws InterruptedException {
        process.waitFor();
    }
}
