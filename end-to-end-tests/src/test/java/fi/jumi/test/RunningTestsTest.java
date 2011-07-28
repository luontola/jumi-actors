// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiLauncher;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.*;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class RunningTestsTest {

    private static final int TIMEOUT = 2000;

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
            System.err.println("WARNING: " + e.getMessage());
        }
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_zero_tests() throws Exception {
        JumiLauncher launcher = new JumiLauncher();
        printProcessOutput(launcher);
        launcher.setJumiHome(sandboxDir);
        launcher.addToClassPath(TestEnvironment.getSampleClasses());
        launcher.setTestsToInclude("sample.notests.*Test");
        launcher.start();
        launcher.awaitSuiteFinished();

        assertThat("total tests", launcher.getTotalTests(), is(0));
    }

    @Test(timeout = TIMEOUT)
    public void suite_with_one_test() throws Exception {
        JumiLauncher launcher = new JumiLauncher();
        printProcessOutput(launcher);
        launcher.setJumiHome(sandboxDir);
        launcher.addToClassPath(TestEnvironment.getSampleClasses());
        launcher.setTestsToInclude("sample.OnePassingTest");
        launcher.start();
        launcher.awaitSuiteFinished();

        assertThat("total tests", launcher.getTotalTests(), is(2)); // test class plus its one test method
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

    // TODO: passing & failing tests
    // TODO: reporting test names
    // TODO: reporting stack traces
}
