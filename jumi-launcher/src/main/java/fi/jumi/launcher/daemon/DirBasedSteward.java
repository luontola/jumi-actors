// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.daemon;

import org.apache.commons.io.IOUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.nio.file.*;

@NotThreadSafe
public class DirBasedSteward implements Steward {

    private final DaemonJar daemonJar;
    private final Path settingsDir; // TODO: default to "~/.jumi" (create default constructor?)

    public DirBasedSteward(DaemonJar daemonJar, Path settingsDir) {
        this.daemonJar = daemonJar;
        this.settingsDir = settingsDir;
    }

    @Override
    public Path getSettingsDir() {
        return settingsDir;
    }

    @Override
    public Path getDaemonJar() {
        try {
            Path extractedJar = settingsDir.resolve("lib/" + daemonJar.getDaemonJarName());
            if (!Files.exists(extractedJar)) {
                InputStream embeddedJar = daemonJar.getDaemonJarAsStream();
                copyToFile(embeddedJar, extractedJar);
            }
            return extractedJar;
        } catch (IOException e) {
            throw new RuntimeException("failed to copy daemon JAR to " + settingsDir, e);
        }
    }

    private static void copyToFile(InputStream in, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        OutputStream out = null;
        try {
            out = Files.newOutputStream(destination);
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }
}
