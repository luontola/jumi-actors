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

@ThreadSafe
public class JumiLauncher implements Closeable {

    private final MessageQueue<Event<SuiteListener>> eventQueue = new MessageQueue<Event<SuiteListener>>();

    private final SuiteOptions suiteOptions = new SuiteOptions();
    private final ActorRef<SuiteLauncher> suiteLauncher;
    private final Closeable externalResources;

    public JumiLauncher(ActorRef<SuiteLauncher> suiteLauncher, Closeable externalResources) {
        this.suiteLauncher = suiteLauncher;
        this.externalResources = externalResources;
    }

    public MessageReceiver<Event<SuiteListener>> getEventStream() {
        return eventQueue;
    }

    public void start() {
        suiteLauncher.tell().runTests(suiteOptions, eventQueue);
    }

    public void shutdownDaemon() {
        suiteLauncher.tell().shutdownDaemon();
    }

    @Override
    public void close() throws IOException {
        externalResources.close();
    }

    // TODO: move these configuration methods to SuiteOptions, pass SuiteOptions as a parameter to start()

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

    public void setStartupTimeout(long startupTimeout) {
        suiteOptions.systemProperties.setProperty(Configuration.STARTUP_TIMEOUT, String.valueOf(startupTimeout));
    }

    public void setIdleTimeout(long idleTimeout) {
        suiteOptions.systemProperties.setProperty(Configuration.IDLE_TIMEOUT, String.valueOf(idleTimeout));
    }
}
