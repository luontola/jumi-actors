// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SynchronizedPrintStreamTest {

    private final ReentrantLock lock = new ReentrantLock();

    @Test
    public void synchronizes_all_methods_on_the_lock_given_as_parameter() {
        SpyOutputStream spy = new SpyOutputStream();
        PrintStream printStream = SynchronizedPrintStream.create(spy, Charset.defaultCharset(), lock);

        printStream.println("foo");

        assertThat("was called", spy.wasCalled, is(true));
        assertThat("used the lock", spy.lockWasHeldByCurrentThread, is(true));
    }


    private class SpyOutputStream extends OutputStream {
        boolean wasCalled = false;
        boolean lockWasHeldByCurrentThread = false;

        @Override
        public void write(int b) {
            wasCalled = true;
            lockWasHeldByCurrentThread = lock.isHeldByCurrentThread();
        }
    }
}
