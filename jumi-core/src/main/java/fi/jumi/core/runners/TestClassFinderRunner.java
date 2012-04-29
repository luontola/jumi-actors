// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.files.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class TestClassFinderRunner implements Runnable {

    private final TestClassFinder testClassFinder;
    private final ActorRef<TestClassFinderListener> finderListener;

    public TestClassFinderRunner(TestClassFinder testClassFinder, ActorRef<TestClassFinderListener> finderListener) {
        this.testClassFinder = testClassFinder;
        this.finderListener = finderListener;
    }

    @Override
    public void run() {
        testClassFinder.findTestClasses(finderListener);
    }
}
