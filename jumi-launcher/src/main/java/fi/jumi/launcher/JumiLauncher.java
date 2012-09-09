// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.*;
import fi.jumi.launcher.remote.SuiteLauncher;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;

@ThreadSafe
public class JumiLauncher implements Closeable {

    private final MessageQueue<Event<SuiteListener>> eventQueue = new MessageQueue<>();

    private final ActorRef<SuiteLauncher> suiteLauncher;
    private final Closeable externalResources;

    public JumiLauncher(ActorRef<SuiteLauncher> suiteLauncher, Closeable externalResources) {
        this.suiteLauncher = suiteLauncher;
        this.externalResources = externalResources;
    }

    public MessageReceiver<Event<SuiteListener>> getEventStream() {
        return eventQueue;
    }

    public void start(SuiteConfiguration suiteConfiguration, DaemonConfiguration daemonConfiguration) {
        suiteLauncher.tell().runTests(suiteConfiguration, daemonConfiguration, eventQueue);
    }

    public void shutdownDaemon() {
        suiteLauncher.tell().shutdownDaemon();
    }

    @Override
    public void close() throws IOException {
        externalResources.close();
    }
}
