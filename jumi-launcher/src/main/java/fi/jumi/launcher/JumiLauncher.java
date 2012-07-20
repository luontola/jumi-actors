// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.remote.SuiteLauncher;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@ThreadSafe
public class JumiLauncher {

    private final MessageQueue<Event<SuiteListener>> eventQueue = new MessageQueue<Event<SuiteListener>>();

    private final SuiteOptions suiteOptions = new SuiteOptions();
    private final ActorRef<SuiteLauncher> suiteLauncher;

    public JumiLauncher(ActorRef<SuiteLauncher> suiteLauncher) {
        this.suiteLauncher = suiteLauncher;
    }

    public MessageReceiver<Event<SuiteListener>> getEventStream() {
        return eventQueue;
    }

    public void start() throws IOException, ExecutionException, InterruptedException {
        suiteLauncher.tell().runTests(suiteOptions, eventQueue);
    }

    public void shutdown() {
        // TODO
    }

    public void addToClassPath(File file) {
        suiteOptions.classPath.add(file);
    }

    public void setTestsToInclude(String pattern) {
        suiteOptions.testsToIncludePattern = pattern;
    }

    public void addJvmOptions(String... jvmOptions) {
        suiteOptions.jvmOptions.addAll(Arrays.asList(jvmOptions));
    }

    public void enableMessageLogging() {
        suiteOptions.systemProperties.setProperty(Configuration.LOG_ACTOR_MESSAGES, "true");
    }
}
