// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.testbench;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.drivers.RunViaAnnotationDriverFinder;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.files.*;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.results.SuiteEventDemuxer;
import fi.jumi.core.runners.SuiteRunner;
import org.apache.commons.io.output.NullOutputStream;

import java.nio.charset.Charset;

public class TestBench {

    public static SuiteEventDemuxer runTests(Class<?>... testClasses) {
        SuiteEventDemuxer results = new SuiteEventDemuxer();

        SingleThreadedActors actors = new SingleThreadedActors(new DynamicEventizerProvider(), new CrashEarlyFailureHandler(), new NullMessageListener());
        SuiteRunner runner = new SuiteRunner(
                new SuiteListenerEventizer().newFrontend(results),
                new FakeTestClassFinder(testClasses),
                new RunViaAnnotationDriverFinder(),
                actors.startActorThread(),
                actors.getExecutor(),
                new OutputCapturer(new NullOutputStream(), new NullOutputStream(), Charset.defaultCharset())
        );
        runner.start();
        actors.processEventsUntilIdle();

        return results;
    }

    private static class FakeTestClassFinder implements TestClassFinder {
        private final Class<?>[] testClasses;

        public FakeTestClassFinder(Class<?>... testClasses) {
            this.testClasses = testClasses;
        }

        @Override
        public void findTestClasses(ActorRef<TestClassFinderListener> listener) {
            for (Class<?> testClass : testClasses) {
                listener.tell().onTestClassFound(testClass);
            }
        }
    }
}
