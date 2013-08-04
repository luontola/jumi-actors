// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

/**
 * Handle for creating and stopping actors.
 */
public interface ActorThread {

    /**
     * Binds an actor to this {@code ActorThread} and returns an {@link ActorRef} for sending messages to it. The
     * recommended pattern for creating actors is {@code ActorRef<TheActorInterface> theActor =
     * actorThread.bindActor(TheActorInterface.class, new TheActor(theParameters));} all in one line.
     * <p>
     * Extra care should be taken to never pass around the raw actor instance. It should always be used through an
     * {@link ActorRef}. Use the convention that when passing an actor as a method/constructor parameter to another
     * class, the type of the parameter is {@code ActorRef<TheActorInterface>}.
     * <p>
     * All actors bound to the same {@code ActorThread} will be executed in the same {@link Thread}, so it is OK for
     * them to share some mutable state when it is known that all the actors are bound to the same thread. A common
     * pattern is to pass an actor its own {@code ActorThread}, so that it can create short-lived actors for callbacks,
     * or a reference to itself, when communicating with other actors.
     * <p>
     * After nobody is holding the actor's {@link ActorRef}, that actor will be garbage collected. It is OK to create
     * lots of short-lived actors.
     */
    <T> ActorRef<T> bindActor(Class<T> type, T rawActor);

    /**
     * Stops <em>all</em> actors which are bound to this {@code ActorThread} after all previously sent messages to them
     * have been processed. It is not possible to stop just one actor from an {@code ActorThread}, though due to garbage
     * collection that is usually not needed.
     * <p>
     * An alternative way to stop actors is to call {@link Thread#interrupt()} on the thread where the actor is running,
     * which will stop that {@code ActorThread} immediately.
     */
    void stop();
}
