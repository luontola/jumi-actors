// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.JumiLauncher;
import fi.jumi.test.util.Threads;
import org.junit.*;

import java.util.List;

import static fi.jumi.test.util.CollectionMatchers.containsAtMost;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReleasingResourcesTest {

    private static final int TIMEOUT = 2000;

    @Rule
    public final AppRunner app = new AppRunner();

    @Ignore("not implemented")
    @Test(timeout = TIMEOUT)
    public void launcher_stops_the_threads_it_started() throws Exception {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        List<Thread> threadsBefore = Threads.getActiveThreads(threadGroup);

        startAndStopLauncher();

        List<Thread> threadsAfter = Threads.getActiveThreads(threadGroup);
        assertThat(threadsAfter, containsAtMost(threadsBefore));
    }

    // TODO: network sockets


    private void startAndStopLauncher() throws Exception {
        app.runTests("unimportant");
        JumiLauncher launcher = app.getLauncher();
        launcher.close();
    }
}
