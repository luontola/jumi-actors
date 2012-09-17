// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.testbench;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.drivers.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.results.SuiteEventDemuxer;
import fi.jumi.core.runners.SuiteRunner;
import org.apache.commons.io.output.NullOutputStream;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class TestBench {

    /**
     * Simulates {@link System#out}
     */
    public final PrintStream out;

    /**
     * Simulates {@link System#err}
     */
    public final PrintStream err;

    private final OutputCapturer outputCapturer;
    private DriverFinder driverFinder = new RunViaAnnotationDriverFinder();
    private MessageListener actorsMessageListener = new NullMessageListener();
    private FailureHandler actorsFailureHandler = new CrashEarlyFailureHandler();

    public TestBench() {
        outputCapturer = new OutputCapturer(new NullOutputStream(), new NullOutputStream(), StandardCharsets.UTF_8);
        out = outputCapturer.out();
        err = outputCapturer.err();
    }

    public SuiteEventDemuxer run(Class<?>... testClasses) {
        SuiteEventDemuxer results = new SuiteEventDemuxer();

        SingleThreadedActors actors = new SingleThreadedActors(
                new DynamicEventizerProvider(),
                actorsFailureHandler,
                actorsMessageListener
        );
        SuiteRunner runner = new SuiteRunner(
                new SuiteListenerEventizer().newFrontend(results),
                new StubTestClassFinder(testClasses),
                driverFinder,
                actors.startActorThread(),
                actors.getExecutor(),
                outputCapturer
        );
        runner.start();
        actors.processEventsUntilIdle();

        return results;
    }


    // setters for changing the defaults

    public void setDriverFinder(DriverFinder driverFinder) {
        this.driverFinder = driverFinder;
    }

    public void setActorsMessageListener(MessageListener actorsMessageListener) {
        this.actorsMessageListener = actorsMessageListener;
    }

    public void setActorsFailureHandler(FailureHandler actorsFailureHandler) {
        this.actorsFailureHandler = actorsFailureHandler;
    }
}
