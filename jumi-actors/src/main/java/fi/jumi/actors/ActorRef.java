// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Handle for communicating with an actor.
 */
@ThreadSafe
public class ActorRef<T> {

    private final T proxy;

    /**
     * Can be used to wrap test doubles into ActorRefs for unit testing purposes.
     * <p/>
     * <span style="color: Red">Warning: Never use this method in production code!
     * This method is meant to be used <em>only</em> from the {@link Actors} class.</span>
     */
    public static <T> ActorRef<T> wrap(T proxy) {
        return new ActorRef<T>(proxy);
    }

    private ActorRef(T proxy) {
        this.proxy = proxy;
    }

    /**
     * Used for sending asynchronous messages to an actor. The recommended usage pattern
     * is {@code actorRef.tell().theMessage(theParameters)}
     * <p/>
     * To avoid confusion, the proxy returned from this method should never be stored
     * in a variable or passed as a parameter to a method. Otherwise it can be hard to know
     * that when you are holding the real actor object and when a proxy to it.
     */
    public T tell() {
        return proxy;
    }
}
