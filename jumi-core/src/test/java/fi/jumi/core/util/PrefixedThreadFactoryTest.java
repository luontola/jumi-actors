// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PrefixedThreadFactoryTest {

    @Test
    public void creates_threads_whose_names_are_the_prefix_and_a_running_number() {
        PrefixedThreadFactory threadFactory = new PrefixedThreadFactory("prefix-");

        Thread thread1 = threadFactory.newThread(new DummyRunnable());
        Thread thread2 = threadFactory.newThread(new DummyRunnable());

        assertThat(thread1.getName()).isEqualTo("prefix-1");
        assertThat(thread2.getName()).isEqualTo("prefix-2");
    }

    private static class DummyRunnable implements Runnable {
        @Override
        public void run() {
        }
    }
}
