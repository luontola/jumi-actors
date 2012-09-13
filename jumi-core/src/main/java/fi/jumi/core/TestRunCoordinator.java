// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.actors.*;
import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.drivers.*;
import fi.jumi.core.files.*;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.runners.SuiteRunner;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class TestRunCoordinator implements CommandListener {

    // TODO: this class is untested and its role is unclear

    private final ActorThread actorThread;
    private final Executor testExecutor;
    private final Runnable shutdownHook;
    private final OutputCapturer outputCapturer;

    private SuiteListener listener = null;

    // XXX: too many constructor parameters, could we group some of them together?
    public TestRunCoordinator(ActorThread actorThread, Executor testExecutor, Runnable shutdownHook, OutputCapturer outputCapturer) {
        this.testExecutor = testExecutor;
        this.actorThread = actorThread;
        this.shutdownHook = shutdownHook;
        this.outputCapturer = outputCapturer;
    }

    @Override
    public void addSuiteListener(SuiteListener listener) {
        // XXX: Setters like this are messy. Could we get rid of this after moving to memory-mapped files?
        this.listener = listener;
    }

    @Override
    public void runTests(SuiteConfiguration suiteConfiguration) {
        TestClassFinder testClassFinder = new FileSystemTestClassFinder(
                suiteConfiguration.classPath(),
                suiteConfiguration.includedTestsPattern()
        );
        DriverFinder driverFinder = new RunViaAnnotationDriverFinder();

        ActorRef<Startable> suiteRunner = actorThread.bindActor(Startable.class,
                new SuiteRunner(listener, testClassFinder, driverFinder, actorThread, testExecutor, outputCapturer));
        suiteRunner.tell().start();
    }

    @Override
    public void shutdown() {
        shutdownHook.run();
    }
}
