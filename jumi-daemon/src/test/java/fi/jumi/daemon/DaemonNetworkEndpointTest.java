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
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class DaemonNetworkEndpointTest {

    private final NetworkConnection unimportantConnection = mock(NetworkConnection.class);
    private final MessageQueue<Event<SuiteListener>> unimportantSender = new MessageQueue<>();
    private final SpyTimeout startupTimeout = new SpyTimeout();
    private final SpyTimeout idleTimeout = new SpyTimeout();

    private final DaemonNetworkEndpoint endpoint = new DaemonNetworkEndpoint(ActorRef.wrap(mock(CommandListener.class)), startupTimeout, idleTimeout);

    @Before
    public void setInitialTimeoutStates() {
        startupTimeout.willTimeOut = true;
        idleTimeout.willTimeOut = false;
    }

    @Test
    public void cancels_the_startup_timeout_when_the_first_user_connects() {
        assertThat("timeout before connected", startupTimeout.willTimeOut, is(true));

        endpoint.onConnected(unimportantConnection, unimportantSender);

        assertThat("timeout after connected", startupTimeout.willTimeOut, is(false));
    }

    @Test
    public void actives_the_idle_timeout_after_the_last_user_disconnects() {
        endpoint.onConnected(unimportantConnection, unimportantSender);
        assertThat("timeout when connected", idleTimeout.willTimeOut, is(false));

        endpoint.onDisconnected();

        assertThat("timeout after disconnected", idleTimeout.willTimeOut, is(true));
    }
}
