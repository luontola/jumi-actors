// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.api.drivers.Driver;
import fi.jumi.core.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.files.TestClassFinder;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.testbench.*;
import fi.jumi.core.util.SpyListener;
import org.apache.commons.io.output.NullOutputStream;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;

public abstract class SuiteRunnerIntegrationHelper {

    // TODO: replace with TestBench?

    private final SpyListener<SuiteListener> spy = new SpyListener<>(SuiteListener.class);
    protected final SuiteListener expect = spy.getListener();

    private final FailureHandler failureHandler = new CrashEarlyFailureHandler();
    private final MessageListener messageListener = new NullMessageListener();
    private final SingleThreadedActors actors = new SingleThreadedActors(new DynamicEventizerProvider(), failureHandler, messageListener);
    private final Executor executor = actors.getExecutor();

    private final OutputCapturer outputCapturer = new OutputCapturer(new NullOutputStream(), new NullOutputStream(), Charset.defaultCharset());
    protected final PrintStream stdout = outputCapturer.out();
    protected final PrintStream stderr = outputCapturer.err();

    protected void runAndCheckExpectations(Driver driver, Class<?>... testClasses) {
        spy.replay();
        run(driver, testClasses);
        spy.verify();
    }

    protected void run(Driver driver, Class<?>... testClasses) {
        run(new StubDriverFinder(driver), testClasses);
    }

    protected void run(DriverFinder driverFinder, Class<?>... testClasses) {
        run(expect, driverFinder, testClasses);
    }

    protected void run(SuiteListener listener, Driver driver, Class<?>... testClasses) {
        run(listener, new StubDriverFinder(driver), testClasses);
    }

    protected void run(SuiteListener listener, DriverFinder driverFinder, Class<?>... testClasses) {
        TestClassFinder testClassFinder = new StubTestClassFinder(testClasses);
        ActorThread actorThread = actors.startActorThread();
        ActorRef<Startable> runner = actorThread.bindActor(Startable.class,
                new SuiteRunner(listener, testClassFinder, driverFinder, actorThread, executor, outputCapturer));
        runner.tell().start();
        actors.processEventsUntilIdle();
    }
}
