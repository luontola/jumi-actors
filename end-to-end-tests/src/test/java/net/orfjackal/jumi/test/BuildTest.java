package net.orfjackal.jumi.test;

import net.orfjackal.jumi.launcher.daemon.Daemon;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.jar.*;

import static org.junit.Assert.assertTrue;

public class BuildTest {

    @Test
    public void in_daemon_jar_all_dependencies_are_hidden_below_jumi_packages() throws IOException {
        List<String> whitelist = Arrays.asList(
                "META-INF/maven/net.orfjackal.jumi/",
                "net/orfjackal/jumi/"
        );

        JarInputStream in = new JarInputStream(Daemon.getDaemonJarAsStream());
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
