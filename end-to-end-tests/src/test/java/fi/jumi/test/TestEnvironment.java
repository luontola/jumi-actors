// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import com.google.common.collect.Iterables;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestEnvironment {

    private static final Path PROJECT_ARTIFACTS_DIR;
    private static final Path SANDBOX_DIR;
    private static final Path SAMPLE_CLASSES_DIR;

    static {
        try (InputStream in = BuildTest.class.getResourceAsStream("/testing.properties")) {
            Properties testing = new Properties();
            testing.load(in);

            PROJECT_ARTIFACTS_DIR = getDirectory(testing, "test.projectArtifactsDir");
            SANDBOX_DIR = getDirectory(testing, "test.sandbox");
            SAMPLE_CLASSES_DIR = getDirectory(testing, "test.sampleClasses");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getDirectory(Properties properties, String key) throws IOException {
        Path path = Paths.get(filteredProperty(properties, key)).toAbsolutePath();
        Files.createDirectories(path);
        return path;
    }

    private static String filteredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value.startsWith("${")) {
            throw new IllegalStateException("the property '" + key + "' was not filled in: " + value);
        }
        return value;
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

    public static Path getSampleClassesDir() {
        return SAMPLE_CLASSES_DIR;
    }
}
