// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class TestRunCoordinatorTest {

    @Test
    public void runs_the_shutdown_hook_when_told_to_shutdown() {
        Runnable shutdownHook = mock(Runnable.class);
        TestRunCoordinator coordinator = new TestRunCoordinator(null, null, shutdownHook, null);

        coordinator.shutdown();

        verify(shutdownHook).run();
    }
}
