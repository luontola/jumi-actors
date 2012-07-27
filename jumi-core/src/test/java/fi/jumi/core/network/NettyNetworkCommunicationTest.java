// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import fi.jumi.actors.queue.MessageSender;
import org.junit.Test;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class NettyNetworkCommunicationTest {

    private static final long TIMEOUT = 2000;
    private static final long ASSERT_TIMEOUT = TIMEOUT - 500; // XXX: using a separate timeout, because JUnit doesn't give a stack trace on timeout

    private final NettyNetworkClient client = new NettyNetworkClient();
    private final NettyNetworkServer server = new NettyNetworkServer();

    private final ClientNetworkEndpoint clientEndpoint = new ClientNetworkEndpoint();
    private final ServerNetworkEndpoint serverEndpoint = new ServerNetworkEndpoint();

    // TODO: disconnect at the end of a test

    @Test(timeout = TIMEOUT)
    public void client_can_send_messages_to_server() throws Exception {
        connectClientToServer();

        clientEndpoint.toServer.get().send(123);

        assertThat(serverEndpoint.messagesReceived.take(), is(123));
    }

    @Test(timeout = TIMEOUT)
    public void server_can_send_messages_to_client() throws Exception {
        connectClientToServer();

        serverEndpoint.toClient.get().send("hello");

        assertThat(clientEndpoint.messagesReceived.take(), is("hello"));
    }

    @Test(timeout = TIMEOUT)
    public void client_can_disconnect() throws Exception {
        connectClientToServer();

        clientEndpoint.connection.get().disconnect();

        assertEventHappens("server should get disconnected event", serverEndpoint.disconnected);
        assertEventHappens("client should get disconnected event", clientEndpoint.disconnected);
    }

    @Test(timeout = TIMEOUT)
    public void server_can_disconnect() throws Exception {
        connectClientToServer();

        serverEndpoint.connection.get().disconnect();

        assertEventHappens("server should get disconnected event", serverEndpoint.disconnected);
        assertEventHappens("client should get disconnected event", clientEndpoint.disconnected);
    }


    private void connectClientToServer() {
        int port = server.listenOnAnyPort(serverEndpoint);
        client.connect("127.0.0.1", port, clientEndpoint);
    }

    private static void assertEventHappens(String message, CountDownLatch event) throws InterruptedException {
        assertTrue(message, event.await(ASSERT_TIMEOUT, TimeUnit.MILLISECONDS));
    }


    private static class ServerNetworkEndpoint implements NetworkEndpoint<Integer, String> {

        public final FutureValue<NetworkConnection> connection = new FutureValue<NetworkConnection>();
        public final FutureValue<MessageSender<String>> toClient = new FutureValue<MessageSender<String>>();
        public final BlockingQueue<Integer> messagesReceived = new LinkedBlockingQueue<Integer>();
        public final CountDownLatch disconnected = new CountDownLatch(1);

        @Override
        public void onConnected(NetworkConnection connection, MessageSender<String> sender) {
            this.connection.set(connection);
            toClient.set(sender);
        }

        @Override
        public void onMessage(Integer message) {
            messagesReceived.add(message);
        }

        @Override
        public void onDisconnected() {
            disconnected.countDown();
        }
    }

    private static class ClientNetworkEndpoint implements NetworkEndpoint<String, Integer> {

        public final FutureValue<NetworkConnection> connection = new FutureValue<NetworkConnection>();
        public final FutureValue<MessageSender<Integer>> toServer = new FutureValue<MessageSender<Integer>>();
        public final BlockingQueue<String> messagesReceived = new LinkedBlockingQueue<String>();
        public final CountDownLatch disconnected = new CountDownLatch(1);

        @Override
        public void onConnected(NetworkConnection connection, MessageSender<Integer> sender) {
            this.connection.set(connection);
            toServer.set(sender);
        }

        @Override
        public void onMessage(String message) {
            messagesReceived.add(message);
        }

        @Override
        public void onDisconnected() {
            disconnected.countDown();
        }
    }
}
