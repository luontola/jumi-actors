// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class ThreadsTest {

    @Test
    public void returns_all_threads_in_a_thread_group() {
        ThreadGroup group = new ThreadGroup("group");
        Thread thread = new Thread(group, new LongRunningTask());
        thread.start();

        assertThat(Threads.getActiveThreads(group), containsInAnyOrder(thread));

        thread.interrupt();
    }

    @Test
    public void returns_all_threads_in_child_thread_groups() {
        ThreadGroup parent = new ThreadGroup("parent");
        ThreadGroup child = new ThreadGroup(parent, "child");
        Thread threadInChild = new Thread(child, new LongRunningTask());
        threadInChild.start();

        assertThat(Threads.getActiveThreads(parent), containsInAnyOrder(threadInChild));

        threadInChild.interrupt();
    }

    @Test
    public void works_when_group_contains_more_threads_than_the_initial_guessimate() {
        final int INITIAL_GUESSIMATE = 2;
        Thread[] threads = new Thread[INITIAL_GUESSIMATE + 2];
        ThreadGroup group = new ThreadGroup("group");
        for (int i = 0; i < threads.length; i++) {
            Thread t = new Thread(group, new LongRunningTask());
            t.start();
            threads[i] = t;
        }

        assertThat(Threads.getActiveThreads(group, INITIAL_GUESSIMATE), containsInAnyOrder(threads));

        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    private static class LongRunningTask implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
    }
}
