// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiLauncher;
import fi.jumi.test.util.Threads;
import org.hamcrest.Matcher;
import org.junit.*;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import static fi.jumi.test.util.CollectionMatchers.containsAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ReleasingResourcesTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Ignore("not implemented")
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void launcher_stops_the_threads_it_started() throws Exception {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        List<Thread> threadsBefore = Threads.getActiveThreads(threadGroup);

        startAndStopLauncher();

        List<Thread> threadsAfter = Threads.getActiveThreads(threadGroup);
        assertThat(threadsAfter, containsAtMost(threadsBefore));
    }

    @Ignore("not implemented")
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void launcher_closes_all_server_sockets_it_opened() throws Exception {
        List<SocketImpl> serverSockets = Collections.synchronizedList(new ArrayList<SocketImpl>());
        ServerSocket.setSocketFactory(new SpySocketImplFactory(serverSockets));

        startAndStopLauncher();

        assertThat("expected the launcher to open server sockets", serverSockets, not(hasSize(0)));
        for (SocketImpl impl : serverSockets) {
            assertIsClosed(getServerSocket(impl));
        }
    }

    @Ignore("not implemented")
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void launcher_closes_all_client_sockets_it_opened() throws Exception {
        List<SocketImpl> clientSockets = Collections.synchronizedList(new ArrayList<SocketImpl>());
        Socket.setSocketImplFactory(new SpySocketImplFactory(clientSockets));

        startAndStopLauncher();

        assertThat("expected the launcher to open client sockets", clientSockets, not(hasSize(0)));
        for (SocketImpl impl : clientSockets) {
            assertIsClosed(getSocket(impl));
        }
    }

    private void startAndStopLauncher() throws Exception {
        app.runTests("unimportant");
        JumiLauncher launcher = app.getLauncher();
        launcher.close();
    }


    // asserts

    private static void assertIsClosed(Socket socket) {
        assertThat(socket.isClosed(), isClosed(socket));
    }

    private static void assertIsClosed(ServerSocket socket) {
        assertThat(socket.isClosed(), isClosed(socket));
    }

    private static Matcher<Boolean> isClosed(Object socket) {
        return describedAs("is closed: %0", is(true), socket);
    }


    // socket helpers

    private static SocketImpl newSocketImpl() {
        try {
            Class<?> defaultSocketImpl = Class.forName("java.net.SocksSocketImpl");
            Constructor<?> constructor = defaultSocketImpl.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (SocketImpl) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Socket getSocket(SocketImpl impl) {
        try {
            Method getSocket = SocketImpl.class.getDeclaredMethod("getSocket");
            getSocket.setAccessible(true);
            return (Socket) getSocket.invoke(impl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ServerSocket getServerSocket(SocketImpl impl) {
        try {
            Method getServerSocket = SocketImpl.class.getDeclaredMethod("getServerSocket");
            getServerSocket.setAccessible(true);
            return (ServerSocket) getServerSocket.invoke(impl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class SpySocketImplFactory implements SocketImplFactory {

        private final List<SocketImpl> spy;

        public SpySocketImplFactory(List<SocketImpl> spy) {
            this.spy = spy;
        }

        @Override
        public SocketImpl createSocketImpl() {
            SocketImpl socket = newSocketImpl();
            spy.add(socket);
            return socket;
        }
    }
}
