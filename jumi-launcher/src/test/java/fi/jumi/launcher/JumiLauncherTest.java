// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.*;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class JumiLauncherTest {

    @Test
    public void on_close_stops_the_actor_thread() {
        final ActorThread actorThread = spy(new FakeActorThread());
        JumiLauncher launcher = new JumiLauncherBuilder() {
            @Override
            protected ActorThread createActorThread(Actors actors) {
                return actorThread;
            }
        }.build();

        launcher.close();

        verify(actorThread).stop();
    }
}
