// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.daemon;

import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.util.Properties;

@Immutable
public class EmbeddedDaemonJar implements DaemonJar {

    private static final String daemonJarName;

    static {
        try (InputStream in = getResourceAsStream("EmbeddedDaemonJar.properties")) {
            Properties p = new Properties();
            p.load(in);
            daemonJarName = p.getProperty("daemonJarName");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDaemonJarName() {
        return daemonJarName;
    }

    @Override
    public InputStream getDaemonJarAsStream() {
        return getResourceAsStream(daemonJarName);
    }

    private static InputStream getResourceAsStream(String resource) {
        InputStream in = EmbeddedDaemonJar.class.getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalArgumentException("resource not found: " + resource);
        }
        return in;
    }
}
