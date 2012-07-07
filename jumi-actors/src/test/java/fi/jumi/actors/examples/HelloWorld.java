// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.examples;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;

import java.util.concurrent.*;

public class HelloWorld {

    public static void main(String[] args) {

        // Configure the actors implementation and its dependencies:
        // - Executor for running the actors (when using MultiThreadedActors)
        // - Eventizers for converting method calls to event objects and back again
        // - Handler for uncaught exceptions from actors
        // - Logging of all messages for debug purposes (here disabled)
        ExecutorService actorsThreadPool = Executors.newCachedThreadPool();
        Actors actors = new MultiThreadedActors(
                actorsThreadPool,
                new DynamicEventizerProvider(),
                new CrashEarlyFailureHandler(),
                new NullMessageListener()
        );

        // Start up a thread where messages to actors will be executed
        ActorThread actorThread = actors.startActorThread();

        // Create an actor to be executed in this actor thread. Some guidelines:
        // - Never pass around a direct reference to an actor, but always use ActorRef
        // - To avoid confusion, also avoid passing around the proxy returned by ActorRef.tell()
        ActorRef<Greeter> helloGreeter = actorThread.bindActor(Greeter.class, new Greeter() {
            @Override
            public void sayGreeting(String name) {
                System.out.println("Hello " + name + " from " + Thread.currentThread().getName());
            }
        });

        // The pattern for sending messages to actors
        helloGreeter.tell().sayGreeting("World");

        // "Wazzup" should be printed before "Hello World" and the thread name will be different
        System.out.println("Wazzup from " + Thread.currentThread().getName());

        // Tell all actors in this actor thread to stop themselves after processing
        // all previously sent messages. An actor can also itself call Thread.currentThread().interrupt()
        actorThread.stop();

        // Finally do an orderly shutdown of the executor.
        // Calling shutdownNow() on the executor would interrupt and stop all
        // actor threads immediately without waiting for messages to be processed.
        actorsThreadPool.shutdown();
    }

    public interface Greeter {

        // Methods on actor interfaces must return void and not have throws declarations.
        // Any parameters may be used, but immutable ones are strongly encouraged.
        // Actors should always be passed around as ActorRefs.
        void sayGreeting(String name);
    }
}
