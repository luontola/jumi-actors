// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestEnvironment {

    private static final Path PROJECT_ARTIFACTS_DIR;
    private static final Path SANDBOX_DIR;
    private static final Path SAMPLE_CLASSES;

    static {
        Properties testing = new Properties();
        InputStream in = BuildTest.class.getResourceAsStream("/testing.properties");
        try {
            testing.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        PROJECT_ARTIFACTS_DIR = Paths.get(testing.getProperty("test.projectArtifactsDir")).toAbsolutePath();
        SANDBOX_DIR = Paths.get(testing.getProperty("test.sandbox")).toAbsolutePath();
        SAMPLE_CLASSES = Paths.get(testing.getProperty("test.sampleClasses")).toAbsolutePath();
    }

    public static Path getProjectJar(String artifactId) throws IOException {
        return getProjectArtifact(artifactId + "-*.jar");
    }

    public static Path getProjectPom(String artifactId) throws IOException {
        return getProjectArtifact(artifactId + "-*.pom");
    }

    private static Path getProjectArtifact(String glob) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(PROJECT_ARTIFACTS_DIR, glob)) {
            try {
                return Iterables.getOnlyElement(stream);
            } catch (NoSuchElementException | IllegalArgumentException e) {
                throw new IllegalArgumentException("could not find the artifact " + glob, e);
            }
        }
    }

    public static Path getSandboxDir() {
        return SANDBOX_DIR;
    }

    public static Path getSampleClasses() {
        return SAMPLE_CLASSES;
    }
}
