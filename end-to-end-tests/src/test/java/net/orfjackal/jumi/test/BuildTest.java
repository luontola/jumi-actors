package net.orfjackal.jumi.test;

import net.orfjackal.jumi.launcher.daemon.Daemon;
import org.apache.commons.io.IOUtils;
import org.junit.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import static org.junit.Assert.assertTrue;

public class BuildTest {

    private static final List<String> JAR_WHITELIST = Arrays.asList(
            "META-INF/maven/net.orfjackal.jumi/",
            "net/orfjackal/jumi/"
    );
    private Properties testing;

    @Before
    public void readProperties() throws IOException {
        testing = new Properties();
        InputStream in = BuildTest.class.getResourceAsStream("/testing.properties");
        try {
            testing.load(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Test
    public void embedded_daemon_jar_contains_only_jumi_classes() throws IOException {
        assertJarContainsOnly(JAR_WHITELIST, Daemon.getDaemonJarAsStream());
    }

    @Test
    public void project_artifact_jars_contain_only_jumi_classes() throws IOException {
        assertJarContainsOnly(JAR_WHITELIST, new FileInputStream(testing.getProperty("test.apiJar")));
        assertJarContainsOnly(JAR_WHITELIST, new FileInputStream(testing.getProperty("test.coreJar")));
        assertJarContainsOnly(JAR_WHITELIST, new FileInputStream(testing.getProperty("test.launcherJar")));
        assertJarContainsOnly(JAR_WHITELIST, new FileInputStream(testing.getProperty("test.daemonJar")));
    }

    private static void assertJarContainsOnly(List<String> whitelist, InputStream jarAsStream) throws IOException {
        JarInputStream in = new JarInputStream(jarAsStream);
        JarEntry entry;
        while ((entry = in.getNextJarEntry()) != null) {
            assertIsWhitelisted(entry, whitelist);
        }
    }

    private static void assertIsWhitelisted(JarEntry entry, List<String> whitelist) {
        boolean allowed = false;
        for (String s : whitelist) {
            allowed |= entry.getName().startsWith(s);
            allowed |= s.startsWith(entry.getName());
        }
        assertTrue("JAR contained a not allowed entry: " + entry, allowed);
    }
}
