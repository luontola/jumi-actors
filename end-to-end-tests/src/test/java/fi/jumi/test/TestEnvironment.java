// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;

public class TestEnvironment {

    private static final File PROJECT_ARTIFACTS_DIR;
    private static final File SANDBOX_DIR;
    private static final File SAMPLE_CLASSES;

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
        PROJECT_ARTIFACTS_DIR = new File(testing.getProperty("test.projectArtifactsDir")).getAbsoluteFile();
        SANDBOX_DIR = new File(testing.getProperty("test.sandbox")).getAbsoluteFile();
        SAMPLE_CLASSES = new File(testing.getProperty("test.sampleClasses")).getAbsoluteFile();
    }

    public static File getProjectJar(final String artifactId) {
        File[] files = PROJECT_ARTIFACTS_DIR.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(artifactId) && name.endsWith(".jar");
            }
        });
        assert files.length == 1 : "expected one file but got " + files.length;
        return files[0];
    }

    public static File getProjectPom(final String artifactId) {
        File[] files = PROJECT_ARTIFACTS_DIR.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(artifactId) && name.endsWith(".pom");
            }
        });
        assert files.length == 1 : "expected one file but got " + files.length;
        return files[0];
    }

    public static File getSandboxDir() {
        return SANDBOX_DIR;
    }

    public static File getSampleClasses() {
        return SAMPLE_CLASSES;
    }
}
