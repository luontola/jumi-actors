// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.listeners;

import java.util.concurrent.Executor;

/**
 * Gets notified about all messages that actors send and receive. Can also listen for all commands submitted to an
 * {@link Executor} by wrapping it in {@link #getListenedExecutor}.
 *
 * @see NullMessageListener
 * @see PrintStreamMessageLogger
 */
public interface MessageListener {

    void onMessageSent(Object message);

    void onProcessingStarted(Object actor, Object message);

    void onProcessingFinished();

    Executor getListenedExecutor(Executor realExecutor);
}
