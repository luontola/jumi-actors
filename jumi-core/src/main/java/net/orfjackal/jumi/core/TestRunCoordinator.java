// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.core.actors.OnDemandActors;
import net.orfjackal.jumi.core.drivers.*;
import net.orfjackal.jumi.core.files.*;
import net.orfjackal.jumi.core.runners.SuiteRunner;

import java.io.File;
import java.util.List;

public class TestRunCoordinator implements CommandListener {

    private final OnDemandActors actors;
    private SuiteListener listener = null;

    public TestRunCoordinator(OnDemandActors actors) {
        this.actors = actors;
    }

    public void addSuiteListener(SuiteListener listener) {
        this.listener = listener;
    }

    public void runTests(final List<File> classPath, final String testsToIncludePattern) {
        TestClassFinder testClassFinder = new FileSystemTestClassFinder(classPath, testsToIncludePattern);
        DriverFinder driverFinder = new RunViaAnnotationDriverFinder();
        SuiteRunner suiteRunner = new SuiteRunner(listener, testClassFinder, driverFinder, actors);

        actors.createSecondaryActor(Startable.class, suiteRunner).start();
    }
}
