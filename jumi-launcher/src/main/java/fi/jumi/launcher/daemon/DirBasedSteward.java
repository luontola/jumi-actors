// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.daemon;

import org.apache.commons.io.IOUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;

@NotThreadSafe
public class DirBasedSteward implements Steward {

    private final DaemonJar daemonJar;
    private final File settingsDir; // TODO: default to "~/.jumi" (create default constructor?)

    public DirBasedSteward(DaemonJar daemonJar, File settingsDir) {
        this.daemonJar = daemonJar;
        this.settingsDir = settingsDir;
    }

    @Override
    public File getSettingsDir() {
        return settingsDir;
    }

    @Override
    public File getDaemonJar() {
        try {
            File extractedJar = new File(settingsDir, "lib/" + daemonJar.getDaemonJarName());
            if (!extractedJar.exists()) {
                InputStream embeddedJar = daemonJar.getDaemonJarAsStream();
                copyToFile(embeddedJar, extractedJar);
            }
            return extractedJar;
        } catch (IOException e) {
            throw new RuntimeException("failed to copy daemon JAR to " + settingsDir, e);
        }
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
}
