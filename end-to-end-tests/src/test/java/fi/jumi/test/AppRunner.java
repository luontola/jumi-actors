// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.actors.Event;
import fi.jumi.actors.queue.MessageQueue;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.runs.RunId;
import fi.jumi.core.util.Strings;
import fi.jumi.launcher.JumiLauncher;
import fi.jumi.launcher.ui.TextUI;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

        String output = out.toString();
        printTextUIOutput(output);
        this.ui = new TextUIParser(output);
    }

    private static void printTextUIOutput(String output) {
        synchronized (System.out) {
            System.out.println("--- TEXT UI OUTPUT ----");
            System.out.println(output);
            System.out.println("--- / TEXT UI OUTPUT ----");
        }
    }

    // assertions

    public void checkPassingAndFailingTests(int expectedPassing, int expectedFailing) {
        assertThat("total tests", ui.getTotalCount(), is(expectedPassing + expectedFailing));
        assertThat("passing tests", ui.getPassingCount(), is(expectedPassing));
        assertThat("failing tests", ui.getFailingCount(), is(expectedFailing));
    }

    public void checkTotalTestRuns(int expectedRunCount) {
        assertThat("total test runs", ui.getRunCount(), is(expectedRunCount));
    }

    public void checkContainsRun(String... startAndEndEvents) {
        List<String> expected = Arrays.asList(startAndEndEvents);
        List<List<String>> actuals = new ArrayList<List<String>>();
        for (RunId runId : ui.getRunIds()) {
            actuals.add(ui.getTestStartAndEndEvents(runId));
        }
        assertThat("did not contain a run with the expected events", actuals, hasItem(expected));
    }

    public void checkHasStackTrace(String... expectedElements) {
        for (RunId id : ui.getRunIds()) {
            String output = ui.getRunOutput(id);
            if (Strings.containsSubStrings(output, expectedElements)) {
                return;
            }
        }
        throw new AssertionError("stack trace not found");
    }

    // JUnit integration

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
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

        if (TestSystemProperties.useThreadSafetyAgent()) {
            String threadSafetyAgent = TestEnvironment.getProjectJar("thread-safety-agent").getAbsolutePath();
            launcher.addJvmOptions("-javaagent:" + threadSafetyAgent);
        }

        launcher.enableMessageLogging();
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
            @Override
            public void write(char[] cbuf, int off, int len) {
                System.out.print(new String(cbuf, off, len));
            }

            @Override
            public void flush() {
                System.out.flush();
            }

            @Override
            public void close() {
                flush();
            }
        });
    }
}
