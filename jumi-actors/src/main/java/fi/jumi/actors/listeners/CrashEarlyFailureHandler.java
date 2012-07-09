// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.listeners;

import fi.jumi.actors.SingleThreadedActors;

import javax.annotation.concurrent.Immutable;

/**
 * Used with {@link SingleThreadedActors} to fail the test when
 * an actor throws an exception.
 */
@Immutable
public class CrashEarlyFailureHandler implements FailureHandler {

    @Override
    public void uncaughtException(Object actor, Object message, Throwable exception) {
        throw new RuntimeException("uncaught exception from " + actor + " when processing message " + message, exception);
    }
}
