// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.failures;

import javax.annotation.concurrent.Immutable;

/**
 * Mean to be used with {@link fi.jumi.actors.SingleThreadedActors}
 * so that tests would crash early when an actor has problems.
 */
@Immutable
public class CrashEarlyFailureHandler implements FailureHandler {

    @Override
    public void uncaughtException(Object actor, Object message, Throwable exception) {
        throw new RuntimeException("uncaught exception from " + actor + " when processing message " + message, exception);
    }
}
