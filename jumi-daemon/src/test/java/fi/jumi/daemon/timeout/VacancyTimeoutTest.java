// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon.timeout;

import org.junit.Test;

import static org.junit.Assert.*;

public class VacancyTimeoutTest {

    private final SpyTimeout spyTimeout = new SpyTimeout();
    private final VacancyTimeout vacancyTimeout = new VacancyTimeout(spyTimeout);

    @Test
    public void does_not_timeout_while_some_are_still_checked_in() {
        vacancyTimeout.checkIn();
        vacancyTimeout.checkIn();
        vacancyTimeout.checkOut();

        assertDoesNotTimeOut();
    }

    @Test
    public void times_out_after_the_last_checkout() {
        vacancyTimeout.checkIn();
        vacancyTimeout.checkOut();

        assertTimesOut();
    }

    @Test
    public void cancels_the_timeout_if_somebody_checks_in_within_the_timeout() {
        vacancyTimeout.checkIn();
        vacancyTimeout.checkOut();
        vacancyTimeout.checkIn();

        assertDoesNotTimeOut();
    }


    // asserts

    private void assertDoesNotTimeOut() {
        assertFalse("expected to NOT time out, but did", spyTimeout.willTimeOut);
    }

    private void assertTimesOut() {
        assertTrue("expected to time out, but did not", spyTimeout.willTimeOut);
    }

    private class SpyTimeout implements Timeout {

        public boolean willTimeOut = false;

        @Override
        public void start() {
            willTimeOut = true;
        }

        @Override
        public void cancel() {
            willTimeOut = false;
        }
    }
}
