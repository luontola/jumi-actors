// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class JumiLauncherTest {

    private static final long TIMEOUT = 1000;

    private ExecutorService actorsThreadPool;

    @Test(timeout = TIMEOUT)
    public void on_close_stops_the_actor_thread() throws IOException {
        final ActorThread actorThread = spy(new FakeActorThread());
        JumiLauncherBuilder builder = new JumiLauncherBuilder() {
            @Override
            protected ActorThread startActorThread(Actors actors) {
                return actorThread;
            }
        };
        JumiLauncher launcher = builder.build();

        launcher.close();

        verify(actorThread).stop();
    }

    @Test(timeout = TIMEOUT)
    public void on_close_terminates_the_actor_thread_pool() throws IOException {
        JumiLauncherBuilder builder = new JumiLauncherBuilder() {
            @Override
            protected ExecutorService createActorsThreadPool() {
                actorsThreadPool = super.createActorsThreadPool();
                return actorsThreadPool;
            }
        };
        JumiLauncher launcher = builder.build();

        launcher.close();

        assertTrue("should have terminated the actors thread pool", actorsThreadPool.isTerminated());
    }
}
