// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.util.Properties;

@Immutable
public class DaemonArtifact {

    private static final String UNKNOWN_VERSION = "<unknown version>";

    public static String getVersion() throws IOException {
        Properties p = new Properties();
        InputStream in = Main.class.getResourceAsStream("/META-INF/maven/fi.jumi/jumi-daemon/pom.properties");
        if (in == null) {
            return UNKNOWN_VERSION;
        }
        try {
            p.load(in);
        } finally {
            in.close();
        }
        return p.getProperty("version", UNKNOWN_VERSION);
    }
}
