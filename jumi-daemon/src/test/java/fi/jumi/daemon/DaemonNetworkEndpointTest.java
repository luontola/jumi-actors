// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageQueue;
import fi.jumi.core.*;
import fi.jumi.core.network.NetworkConnection;
import fi.jumi.daemon.timeout.SpyTimeout;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class DaemonNetworkEndpointTest {

    @Test
    public void actives_the_idle_timeout_after_the_last_user_disconnects() {
        SpyTimeout idleTimeout = new SpyTimeout();
        DaemonNetworkEndpoint endpoint = new DaemonNetworkEndpoint(ActorRef.wrap(mock(CommandListener.class)), idleTimeout);
        endpoint.onConnected(mock(NetworkConnection.class), new MessageQueue<Event<SuiteListener>>());
        assertThat("timeout when connected", idleTimeout.willTimeOut, is(false));

        endpoint.onDisconnected();

        assertThat("timeout after disconnected", idleTimeout.willTimeOut, is(true));
    }
}
