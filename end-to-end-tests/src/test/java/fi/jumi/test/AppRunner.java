// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.core.config.*;
import fi.jumi.core.network.*;
import fi.jumi.core.runs.RunId;
import fi.jumi.core.util.Strings;
import fi.jumi.launcher.*;
import fi.jumi.launcher.process.*;
import fi.jumi.launcher.ui.*;
import fi.jumi.test.util.CloseAwaitableStringWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.*;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

public class AppRunner implements TestRule {

    // TODO: use a proper sandbox utility
    private final Path sandboxDir = TestEnvironment.getSandboxDir().resolve(UUID.randomUUID().toString());

    private final SpyProcessStarter processStarter = new SpyProcessStarter(new SystemProcessStarter());
    private final CloseAwaitableStringWriter daemonOutput = new CloseAwaitableStringWriter();
    private NetworkServer mockNetworkServer = null;
    private Charset daemonDefaultCharset = StandardCharsets.UTF_8;

    private JumiLauncher launcher;
    private TextUIParser ui;

    public final SuiteConfigurationBuilder suite = new SuiteConfigurationBuilder();
    public final DaemonConfigurationBuilder daemon = new DaemonConfigurationBuilder();

    public void setMockNetworkServer(NetworkServer mockNetworkServer) {
        this.mockNetworkServer = mockNetworkServer;
    }

    public void setDaemonDefaultCharset(Charset daemonDefaultCharset) {
        this.daemonDefaultCharset = daemonDefaultCharset;
    }

    public JumiLauncher getLauncher() {
        if (launcher == null) {
            launcher = createLauncher();
        }
        return launcher;
    }

    private JumiLauncher createLauncher() {
        class CustomJumiLauncherBuilder extends JumiLauncherBuilder {

            @Override
            protected Path getSettingsDirectory() {
                return sandboxDir.resolve("jumi-home");
            }

            @Override
            protected ProcessStarter createProcessStarter() {
                return processStarter;
            }

            @Override
            protected NetworkServer createNetworkServer() {
                if (mockNetworkServer != null) {
                    return mockNetworkServer;
                }
                return super.createNetworkServer();
            }

            @Override
            protected OutputStream createDaemonOutputListener() {
                return new TeeOutputStream(
                        new CloseShieldOutputStream(System.out),
                        new WriterOutputStream(daemonOutput, daemonDefaultCharset));
            }
        }

        JumiLauncherBuilder builder = new CustomJumiLauncherBuilder();
        return builder.build();
    }

    public Process getDaemonProcess() throws Exception {
        return processStarter.lastProcess.get();
    }

    public String getFinishedDaemonOutput() throws InterruptedException {
        daemonOutput.await();
        return getCurrentDaemonOutput();
    }

    public String getCurrentDaemonOutput() throws InterruptedException {
        return daemonOutput.toString();
    }

    public void runTests(Class<?> clazz) throws Exception {
        runTests(clazz.getName());
    }

    public void runTests(String testsToInclude) throws Exception {
        startTests(testsToInclude);

        StringBuilder outputBuffer = new StringBuilder();
        TextUI ui = new TextUI(launcher.getEventStream(), new PlainTextPrinter(outputBuffer));
        ui.updateUntilFinished();

        String output = outputBuffer.toString();
        printTextUIOutput(output);
        this.ui = new TextUIParser(output);
    }

    public void startTests(String testsToInclude) throws IOException {
        getLauncher().start(configure(suite.freeze(), testsToInclude), configure(daemon.freeze()));
    }

    private SuiteConfiguration configure(SuiteConfiguration suite, String testsToInclude) throws IOException {
        SuiteConfigurationBuilder builder = suite.melt();

        builder.addJvmOptions("-Dfile.encoding=" + daemonDefaultCharset.name());
        builder.addToClassPath(TestEnvironment.getProjectJar("simpleunit"));
        builder.addToClassPath(TestEnvironment.getSampleClassesDir());
        builder.includedTestsPattern(testsToInclude);
        if (TestSystemProperties.useThreadSafetyAgent()) {
            builder.addJvmOptions("-javaagent:" + TestEnvironment.getProjectJar("thread-safety-agent"));
        }
        return builder.freeze();
    }

    private DaemonConfiguration configure(DaemonConfiguration daemon) {
        return daemon.melt()
                .logActorMessages(true)
                .freeze();
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
        assertNotNull("did not contain a run with the expected events", findRun(startAndEndEvents));
    }

    public RunId findRun(String... startAndEndEvents) {
        List<String> expectedEvents = Arrays.asList(startAndEndEvents);
        for (RunId runId : ui.getRunIds()) {
            List<String> actualEvents = ui.getTestStartAndEndEvents(runId);
            if (actualEvents.equals(expectedEvents)) {
                return runId;
            }
        }
        return null;
    }

    public String getRunOutput(RunId runId) {
        return ui.getRunOutput(runId);
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
                setup();
                try {
                    base.evaluate();
                } finally {
                    tearDown();
                }
            }
        };
    }

    private void setup() throws IOException {
        Files.createDirectories(sandboxDir);
    }

    private void tearDown() {
        if (launcher != null) {
            try {
                launcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (processStarter.lastProcess.isDone()) {
            try {
                Process process = processStarter.lastProcess.get();
                kill(process);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            FileUtils.forceDelete(sandboxDir.toFile());
        } catch (IOException e) {
            System.err.println("WARNING: " + e.getMessage());
        }
    }

    private static void kill(Process process) {
        process.destroy();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    // helpers

    public static class SpyProcessStarter implements ProcessStarter {

        private final ProcessStarter processStarter;
        public final FutureValue<Process> lastProcess = new FutureValue<>();

        public SpyProcessStarter(ProcessStarter processStarter) {
            this.processStarter = processStarter;
        }

        @Override
        public Process startJavaProcess(JvmArgs jvmArgs) throws IOException {
            Process process = processStarter.startJavaProcess(jvmArgs);
            lastProcess.set(process);
            return process;
        }
    }
}
