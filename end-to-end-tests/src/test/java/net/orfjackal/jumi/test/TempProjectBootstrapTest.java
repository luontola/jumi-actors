// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.test;

import net.orfjackal.jumi.launcher.JumiLauncher;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.*;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class TempProjectBootstrapTest {

    // TODO: use a proper sandbox utility
    private final File sandboxDir = new File(TestEnvironment.getSandboxDir(), UUID.randomUUID().toString());

    @Before
    public void createSandbox() throws IOException {
        assertTrue("Unable to create " + sandboxDir, sandboxDir.mkdirs());
    }

    @After
    public void deleteSandbox() throws IOException {
        try {
            // XXX: may fail to delete because the daemon is still running and the JAR is locked
            FileUtils.forceDelete(sandboxDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void starts_daemon_in_a_new_process() throws Exception {
        StringWriter out = new StringWriter();

        JumiLauncher launcher = new JumiLauncher();
        launcher.setJumiHome(sandboxDir);
        launcher.setOutputListener(out);
        launcher.start();
        launcher.awaitSuiteFinished();

        assertThat(out.toString(), startsWith("Hello world"));
        System.out.println(out.toString());
    }

    @Test
    public void suite_with_zero_tests() throws Exception {
        JumiLauncher launcher = new JumiLauncher();
        launcher.setJumiHome(sandboxDir);
        launcher.addToClassPath(TestEnvironment.getSampleClasses());
        launcher.setTestsToInclude("sample.notests.*Test");
        launcher.start();
        launcher.awaitSuiteFinished();

        assertThat(launcher.getTotalTests(), is(0));
    }
}
