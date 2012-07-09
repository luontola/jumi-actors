// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.listeners;

/**
 * Gets notified about uncaught exceptions thrown by actors.
 *
 * @see PrintStreamFailureLogger
 * @see CrashEarlyFailureHandler
 */
public interface FailureHandler {

    /**
     * Should log the exception and possibly do some error recovery.
     * <p/>
     * May stop the actor thread by interrupting the current thread. Otherwise the actor thread (and all actors in it)
     * will keep on processing messages.
     * <p/>
     * Should not throw any exceptions - that would result in implementation specific behaviour.
     */
    void uncaughtException(Object actor, Object message, Throwable exception);
}
