// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon.timeout;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
public class VacancyTimeout {

    private final AtomicInteger checkedIn = new AtomicInteger(0);
    private final Timeout timeout;

    public VacancyTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public void checkIn() {
        checkedIn.incrementAndGet();
        timeout.cancel();
    }

    public void checkOut() {
        int checkedIn = this.checkedIn.decrementAndGet();
        assert checkedIn >= 0 : checkedIn;
        if (checkedIn == 0) {
            timeout.start();
        }
    }
}
