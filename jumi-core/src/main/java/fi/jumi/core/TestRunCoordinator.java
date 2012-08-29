// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.actors.*;
import fi.jumi.core.drivers.*;
import fi.jumi.core.files.*;
import fi.jumi.core.runners.SuiteRunner;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;

@NotThreadSafe
public class TestRunCoordinator implements CommandListener {

    // TODO: this class is untested and its role is unclear

    private final ActorThread actorThread;
    private final Executor testExecutor;
    private final Runnable shutdownHook;

    private SuiteListener listener = null;

    public TestRunCoordinator(ActorThread actorThread, Executor testExecutor, Runnable shutdownHook) {
        this.testExecutor = testExecutor;
        this.actorThread = actorThread;
        this.shutdownHook = shutdownHook;
    }

    @Override
    public void addSuiteListener(SuiteListener listener) {
        // XXX: Setters like this are messy. Could we get rid of this after moving to memory-mapped files?
        this.listener = listener;
    }

    @Override
    public void runTests(final List<File> classPath, final String testsToIncludePattern) {
        TestClassFinder testClassFinder = new FileSystemTestClassFinder(classPath, testsToIncludePattern);
        DriverFinder driverFinder = new RunViaAnnotationDriverFinder();

        ActorRef<Startable> suiteRunner = actorThread.bindActor(Startable.class,
                new SuiteRunner(listener, testClassFinder, driverFinder, actorThread, testExecutor));
        suiteRunner.tell().start();
    }

    @Override
    public void shutdown() {
        shutdownHook.run();
    }
}
