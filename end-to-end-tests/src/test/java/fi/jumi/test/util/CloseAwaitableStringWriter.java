// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import java.io.StringWriter;
import java.util.concurrent.*;

public class CloseAwaitableStringWriter extends StringWriter {

    private final CountDownLatch finished = new CountDownLatch(1);

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return finished.await(timeout, unit);
    }

    public void await() throws InterruptedException {
        finished.await();
    }

    @Override
    public void close() {
        finished.countDown();
    }
}
