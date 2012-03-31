// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.actors.*;
import fi.jumi.core.*;
import fi.jumi.launcher.JumiLauncher;
import fi.jumi.launcher.ui.TextUI;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.*;
import java.util.UUID;

import static fi.jumi.core.utils.Asserts.assertContainsSubStrings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class AppRunner implements TestRule {

    // TODO: use a proper sandbox utility
    private final File sandboxDir = new File(TestEnvironment.getSandboxDir(), UUID.randomUUID().toString());

    private final MessageQueue<Event<SuiteListener>> eventStream = new MessageQueue<Event<SuiteListener>>();

    private final JumiLauncher launcher = new JumiLauncher(eventStream);
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private TextUIParser ui;

    public void runTests(Class<?> clazz) throws IOException, InterruptedException {
        runTests(clazz.getName());
    }

    public void runTests(String testsToInclude) throws IOException, InterruptedException {
        launcher.addToClassPath(TestEnvironment.getSampleClasses());
        launcher.setTestsToInclude(testsToInclude);
        launcher.start();

        TextUI ui = new TextUI(new PrintStream(out), new PrintStream(out), eventStream);
        ui.updateUntilFinished();

        synchronized (System.out) {
            String output = out.toString();
            System.out.println("--- TEXT UI OUTPUT ----");
            System.out.println(output);
            System.out.println("--- / TEXT UI OUTPUT ----");
            this.ui = new TextUIParser(output);
        }
    }

    public void checkTotalTests(int expected) {
        assertThat("total tests", ui.getTotalCount(), is(expected));
    }

    public void checkFailingTests(int expected) {
        assertThat("failing tests", ui.getFailingCount(), is(expected));
    }

    public void checkPassingTests(int expected) {
        assertThat("passing tests", ui.getPassingCount(), is(expected));
    }

    public void checkHasStackTrace(RunId runId, String... expectedElements) {
        String actual = ui.getRunOutput(runId);
        assertContainsSubStrings("stack trace not found", actual, expectedElements);
    }

    // JUnit integration

    @Override
    public Statement apply(final Statement base, Description description) {
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
