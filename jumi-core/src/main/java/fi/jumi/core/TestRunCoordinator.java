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

    private final OnDemandActors actors;
    private final Executor executor;
    private SuiteListener listener = null;

    public TestRunCoordinator(OnDemandActors actors, Executor executor) {
        this.actors = actors;
        this.executor = executor;
    }

    @Override
    public void addSuiteListener(SuiteListener listener) {
        this.listener = listener;
    }

    @Override
    public void runTests(final List<File> classPath, final String testsToIncludePattern) {
        TestClassFinder testClassFinder = new FileSystemTestClassFinder(classPath, testsToIncludePattern);
        DriverFinder driverFinder = new RunViaAnnotationDriverFinder();

        ActorRef<Startable> suiteRunner = actors.createSecondaryActor(Startable.class,
                new SuiteRunner(listener, testClassFinder, driverFinder, actors, executor));
        suiteRunner.tell().start();
    }
}
