// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import fi.jumi.actors.queue.MessageSender;
import org.junit.*;

import java.util.Collections;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class NettyNetworkCommunicationTest {

    private static final long TIMEOUT = 1000;
    private static final long ASSERT_TIMEOUT = 500;
    private static final boolean LOGGING = false;

    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
    private final ExecutorService serverExecutor = Executors.newCachedThreadPool();

    private final NettyNetworkClient client = new NettyNetworkClient(LOGGING, clientExecutor);
    private final NettyNetworkServer server = new NettyNetworkServer(LOGGING, serverExecutor);

    private final ClientNetworkEndpoint clientEndpoint = new ClientNetworkEndpoint();
    private final ServerNetworkEndpoint serverEndpoint = new ServerNetworkEndpoint();

    @After
    public void tearDown() {
        client.close();
        server.close();
    }


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
    public void multiple_clients_can_connect_to_the_server_independently() throws Exception {
        ServerNetworkEndpoint serverEndpoint1 = new ServerNetworkEndpoint();
        ServerNetworkEndpoint serverEndpoint2 = new ServerNetworkEndpoint();
        int port = server.listenOnAnyPort(new StubServerNetworkEndpointFactory(serverEndpoint1, serverEndpoint2));

        ClientNetworkEndpoint clientEndpoint1 = new ClientNetworkEndpoint();
        client.connect("127.0.0.1", port, clientEndpoint1);
        ClientNetworkEndpoint clientEndpoint2 = new ClientNetworkEndpoint();
        client.connect("127.0.0.1", port, clientEndpoint2);

        serverEndpoint1.toClient.get().send("message1");
        clientEndpoint1.toServer.get().send(100);
        serverEndpoint2.toClient.get().send("message2");
        clientEndpoint2.toServer.get().send(200);

        assertThat(clientEndpoint1.messagesReceived.take(), is("message1"));
        assertThat(serverEndpoint1.messagesReceived.take(), is(100));
        assertThat(clientEndpoint2.messagesReceived.take(), is("message2"));
        assertThat(serverEndpoint2.messagesReceived.take(), is(200));
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

    @Test(timeout = TIMEOUT)
    public void on_close_the_client_disconnects_all_connections() throws Exception {
        connectClientToServer();

        client.close();

        assertEventHappens("client should get disconnected event", clientEndpoint.disconnected);
    }

    @Test(timeout = TIMEOUT)
    public void on_close_the_server_disconnects_all_connections() throws Exception {
        connectClientToServer();
        serverEndpoint.connection.get();

        server.close();

        assertEventHappens("server should get disconnected event", serverEndpoint.disconnected);
    }

    @Test(timeout = TIMEOUT)
    public void on_close_the_client_terminates_its_executors() {
        connectClientToServer();

        client.close();

        assertThat("client executor terminated", clientExecutor.isTerminated(), is(true));
    }

    @Test(timeout = TIMEOUT)
    public void on_close_the_server_terminates_its_executors() {
        connectClientToServer();

        server.close();

        assertThat("server executor terminated", serverExecutor.isTerminated(), is(true));
    }


    private void connectClientToServer() {
        int port = server.listenOnAnyPort(new StubServerNetworkEndpointFactory(serverEndpoint));
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

    private static class StubServerNetworkEndpointFactory implements NetworkEndpointFactory<Integer, String> {
        private final BlockingQueue<ServerNetworkEndpoint> serverEndpoints;

        public StubServerNetworkEndpointFactory(ServerNetworkEndpoint... serverEndpoints) {
            this.serverEndpoints = new ArrayBlockingQueue<ServerNetworkEndpoint>(serverEndpoints.length);
            Collections.addAll(this.serverEndpoints, serverEndpoints);
        }

        @Override
        public NetworkEndpoint<Integer, String> createEndpoint() {
            ServerNetworkEndpoint endpoint = serverEndpoints.poll();
            assertNotNull("more clients connected than were expected", endpoint);
            return endpoint;
        }
    }
}
