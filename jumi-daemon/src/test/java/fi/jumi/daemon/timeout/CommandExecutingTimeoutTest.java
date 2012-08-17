// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon.timeout;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CommandExecutingTimeoutTest {

    private static final long TEST_TIMEOUT = 1000;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private CommandExecutingTimeout timeout;
    private final AtomicInteger numberOfTimeouts = new AtomicInteger(0);

    @Test(timeout = TEST_TIMEOUT)
    public void runs_the_command_after_the_timeout() throws InterruptedException {
        timeout = new CommandExecutingTimeout(new SpyCommand(), 1, TimeUnit.MILLISECONDS);

        timeout.start();

        assertNumberOfTimeouts(1);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void does_not_run_the_command_if_cancelled_before_the_timeout() throws InterruptedException {
        timeout = new CommandExecutingTimeout(new SpyCommand(), 100, TimeUnit.MILLISECONDS);

        timeout.start();
        timeout.cancel();

        assertNumberOfTimeouts(0);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void the_timeout_can_be_restarted() throws InterruptedException {
        timeout = new CommandExecutingTimeout(new SpyCommand(), 10, TimeUnit.MILLISECONDS);

        timeout.start();
        timeout.cancel();
        timeout.start();

        assertNumberOfTimeouts(1);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void cancelling_is_idempotent() throws InterruptedException {
        timeout = new CommandExecutingTimeout(new SpyCommand(), 100, TimeUnit.MILLISECONDS);

        timeout.cancel();
        timeout.cancel();
        timeout.start();
        timeout.cancel();
        timeout.cancel();

        assertNumberOfTimeouts(0);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void cannot_start_many_times_without_cancelling_first() throws InterruptedException {
        timeout = new CommandExecutingTimeout(new SpyCommand(), 10, TimeUnit.MILLISECONDS);

        timeout.start();

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("already started");
        timeout.start();
    }


    private void assertNumberOfTimeouts(int expected) throws InterruptedException {
        waitForPossibleTimeout();
        assertThat(numberOfTimeouts.get(), is(expected));
    }

    private void waitForPossibleTimeout() throws InterruptedException {
        Thread thread = timeout.scheduler;
        if (thread != null) {
            thread.join();
        }
    }

    private class SpyCommand implements Runnable {
        @Override
        public void run() {
            numberOfTimeouts.incrementAndGet();
        }
    }
}
