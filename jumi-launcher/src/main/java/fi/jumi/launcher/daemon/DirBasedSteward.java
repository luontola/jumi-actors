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
        Path extractedJar = settingsDir.resolve("lib/" + daemonJar.getDaemonJarName());
        createIfDoesNotExist(extractedJar);
        return extractedJar;
    }

    private void createIfDoesNotExist(Path extractedJar) {
        if (Files.exists(extractedJar)) {
            return;
        }
        try (InputStream embeddedJar = daemonJar.getDaemonJarAsStream()) {
            copyToFile(embeddedJar, extractedJar);
        } catch (IOException e) {
            throw new RuntimeException("failed to copy the embedded daemon JAR to " + extractedJar, e);
        }
    }

    private static void copyToFile(InputStream source, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        try (OutputStream out = Files.newOutputStream(destination)) {
            IOUtils.copy(source, out);
        }
    }
}
