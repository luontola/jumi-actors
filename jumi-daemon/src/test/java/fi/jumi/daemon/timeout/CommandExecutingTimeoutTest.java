// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon.timeout;

import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.*;

public class CommandExecutingTimeoutTest {

    private static final long TEST_TIMEOUT = 1000;
    private static final long ASSERT_TIMEOUT = 500;

    private final CountDownLatch timedOut = new CountDownLatch(1);

    @Test(timeout = TEST_TIMEOUT)
    public void runs_the_command_after_the_timeout() throws InterruptedException {
        Timeout timeout = new CommandExecutingTimeout(new SpyCommand(), 0, TimeUnit.MILLISECONDS);

        timeout.start();

        assertTimesOut();
    }

    @Test(timeout = TEST_TIMEOUT)
    public void does_not_run_the_command_if_cancelled_before_the_timeout() throws InterruptedException {
        Timeout timeout = new CommandExecutingTimeout(new SpyCommand(), ASSERT_TIMEOUT / 2, TimeUnit.MILLISECONDS);

        timeout.start();
        timeout.cancel();

        assertDoesNotTimeOut();
    }


    private void assertDoesNotTimeOut() throws InterruptedException {
        assertFalse("expected to NOT time out, but it did", timedOut.await(1, TimeUnit.MILLISECONDS));
    }

    private void assertTimesOut() throws InterruptedException {
        assertTrue("expected to time out, but it did not", timedOut.await(ASSERT_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    private class SpyCommand implements Runnable {
        @Override
        public void run() {
            timedOut.countDown();
        }
    }
}
