// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiLauncher;
import org.apache.commons.io.FileUtils;
import org.junit.rules.MethodRule;
import org.junit.runners.model.*;

import java.io.*;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class AppRunner implements MethodRule {

    // TODO: use a proper sandbox utility
    private final File sandboxDir = new File(TestEnvironment.getSandboxDir(), UUID.randomUUID().toString());

    public final JumiLauncher launcher = new JumiLauncher();

    public Statement apply(final Statement base, FrameworkMethod method, Object target) {
        return new Statement() {
            public void evaluate() throws Throwable {
                setUp();
                try {
                    base.evaluate();
                } finally {
                    tearDown();
                }
            }
        };
    }

    private void setUp() {
        assertTrue("Unable to create " + sandboxDir, sandboxDir.mkdirs());

        printProcessOutput(launcher);
        launcher.setJumiHome(sandboxDir);

        if (System.getProperty("jumi.useThreadSafetyAgent", "false").equals("true")) {
            String threadSafetyAgent = TestEnvironment.getProjectJar("thread-safety-agent").getAbsolutePath();
            launcher.setJvmOptions("-javaagent:" + threadSafetyAgent);
        }
    }

    private void tearDown() {
        try {
            // XXX: may fail to delete because the daemon is still running and the JAR is locked
            FileUtils.forceDelete(sandboxDir);
        } catch (IOException e) {
            System.err.println("WARNING: " + e.getMessage());
        }
    }

    private static void printProcessOutput(JumiLauncher launcher) {
        launcher.setOutputListener(new Writer() {
            public void write(char[] cbuf, int off, int len) {
                System.out.print(new String(cbuf, off, len));
            }

            public void flush() {
                System.out.flush();
            }

            public void close() {
                flush();
            }
        });
    }
}
