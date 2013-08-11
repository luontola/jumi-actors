// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.luontola.buildtest.*;

import java.io.File;
import java.util.Properties;

public class TestEnvironment {

    public static final VersionNumbering VERSION_NUMBERING = new VersionNumbering();
    public static final ProjectArtifacts ARTIFACTS;

    static {
        Properties p = ResourcesUtil.getProperties("testing.properties");
        ARTIFACTS = new ProjectArtifacts(getDirectory(p, "test.projectArtifactsDir"));
    }

    private static File getDirectory(Properties properties, String key) {
        File file = new File(filteredProperty(properties, key));
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + file);
        }
        return file;
    }

    private static String filteredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value.startsWith("${")) {
            throw new IllegalStateException("the property '" + key + "' was not filled in: " + value);
        }
        return value;
    }
}
