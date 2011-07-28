// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.daemon;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;

public class Daemon {

    private static final String daemonJarName;

    static {
        InputStream in = getResourceAsStream("daemon.properties");
        try {
            Properties p = new Properties();
            p.load(in);
            daemonJarName = p.getProperty("daemonJarName");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static String getDaemonJarName() {
        return daemonJarName;
    }

    public static InputStream getDaemonJarAsStream() {
        return getResourceAsStream(daemonJarName);
    }

    private static InputStream getResourceAsStream(String resource) {
        InputStream in = Daemon.class.getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalArgumentException("resource not found: " + resource);
        }
        return in;
    }
}
