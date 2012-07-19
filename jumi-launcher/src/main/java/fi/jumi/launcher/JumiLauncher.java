// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.SuiteListener;
import fi.jumi.core.config.Configuration;
import fi.jumi.launcher.daemon.HomeManager;
import fi.jumi.launcher.network.DaemonConnector;
import fi.jumi.launcher.process.ProcessStarter;
import fi.jumi.launcher.remote.*;
import org.apache.commons.io.output.NullWriter;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@ThreadSafe
public class JumiLauncher {

    private final MessageQueue<Event<SuiteListener>> eventQueue = new MessageQueue<Event<SuiteListener>>();
    private final ActorThread actorThread;
    private final HomeManager homeManager;
    private final DaemonConnector daemonConnector;
    private final ProcessStarter processStarter;

    private SuiteOptions suiteOptions = new SuiteOptions();
    private Writer outputListener = new NullWriter();

    // TODO: create default constructor or helper factory method for default configuration?
    public JumiLauncher(ActorThread actorThread,
                        HomeManager homeManager,
                        DaemonConnector daemonConnector,
                        ProcessStarter processStarter) {
        this.actorThread = actorThread;
        this.homeManager = homeManager;
        this.daemonConnector = daemonConnector;
        this.processStarter = processStarter;
    }

    public MessageReceiver<Event<SuiteListener>> getEventStream() {
        return eventQueue;
    }

    public void start() throws IOException, ExecutionException, InterruptedException {
        // TODO: move creating the actors outside this class?
        ActorRef<DaemonRemote> daemonRemote = actor(new DaemonRemoteImpl(
                homeManager,
                processStarter,
                daemonConnector,
                outputListener
        ));
        ActorRef<SuiteRemote> suiteRemote = actor(new SuiteRemoteImpl(actorThread, daemonRemote));

        suiteRemote.tell().runTests(suiteOptions, eventQueue);
    }

    public void shutdown() {
        // TODO
    }

    public void setOutputListener(Writer outputListener) {
        this.outputListener = outputListener;
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


    // actor helpers

    private ActorRef<SuiteRemote> actor(SuiteRemote rawActor) {
        return actorThread.bindActor(SuiteRemote.class, rawActor);
    }

    private ActorRef<DaemonRemote> actor(DaemonRemote rawActor) {
        return actorThread.bindActor(DaemonRemote.class, rawActor);
    }
}
