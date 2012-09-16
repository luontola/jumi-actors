// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OutputCapturerTest {

    private static final long TIMEOUT = 1000;

    private final StringWriter printedToOut = new StringWriter();
    private final StringWriter printedToErr = new StringWriter();
    private final OutputStream realOut = new WriterOutputStream(printedToOut);
    private final OutputStream realErr = new WriterOutputStream(printedToErr);

    private final OutputCapturer capturer = new OutputCapturer(realOut, realErr, Charset.defaultCharset());


    // basic capturing

    @Test
    public void passes_through_stdout_to_the_real_stdout() {
        capturer.out().print("foo");

        assertThat("stdout", printedToOut.toString(), is("foo"));
        assertThat("stderr", printedToErr.toString(), is(""));
    }

    @Test
    public void passes_through_stderr_to_the_real_stderr() {
        capturer.err().print("foo");

        assertThat("stdout", printedToOut.toString(), is(""));
        assertThat("stderr", printedToErr.toString(), is("foo"));
    }

    @Test
    public void captures_stdout() {
        OutputListenerSpy listener = new OutputListenerSpy();

        capturer.captureTo(listener);
        capturer.out().print("foo");

        assertThat(listener.out).as("stdout").containsExactly("foo");
        assertThat(listener.err).as("stderr").containsExactly();
    }

    @Test
    public void captures_stderr() {
        OutputListenerSpy listener = new OutputListenerSpy();

        capturer.captureTo(listener);
        capturer.err().print("foo");

        assertThat(listener.out).as("stdout").containsExactly();
        assertThat(listener.err).as("stderr").containsExactly("foo");
    }

    @Test
    public void single_byte_prints_are_also_captured_and_passed_through() {
        OutputListenerSpy listener = new OutputListenerSpy();
        capturer.captureTo(listener);

        capturer.out().write('.');

        assertThat(printedToOut.toString(), is("."));
        assertThat(listener.out).containsExactly(".");
    }

    @Test
    public void after_starting_a_new_capture_all_new_events_to_to_the_new_output_listener() {
        OutputListenerSpy listener1 = new OutputListenerSpy();
        OutputListenerSpy listener2 = new OutputListenerSpy();

        capturer.captureTo(listener1);
        capturer.captureTo(listener2);
        capturer.out().print("foo");

        assertThat(listener1.out).containsExactly();
        assertThat(listener2.out).containsExactly("foo");
    }

    @Test
    public void starting_a_new_capture_does_not_require_installing_a_new_PrintStream_to_SystemOut() {
        OutputListenerSpy listener = new OutputListenerSpy();

        PrintStream out = capturer.out();
        capturer.captureTo(listener);
        out.print("foo");

        assertThat(listener.out).containsExactly("foo");
    }


    // concurrency

    @Test(timeout = TIMEOUT)
    public void concurrent_captures_are_isolated_from_each_other() throws InterruptedException {
        final CountDownLatch barrier = new CountDownLatch(2);
        final OutputListenerSpy listener1 = new OutputListenerSpy();
        final OutputListenerSpy listener2 = new OutputListenerSpy();

        runConcurrently(
                new Runnable() {
                    @Override
                    public void run() {
                        capturer.captureTo(listener1);
                        sync(barrier);
                        capturer.out().print("from thread 1");
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        capturer.captureTo(listener2);
                        sync(barrier);
                        capturer.out().print("from thread 2");
                    }
                }
        );

        assertThat(listener1.out).containsExactly("from thread 1");
        assertThat(listener2.out).containsExactly("from thread 2");
    }

    @Test(timeout = TIMEOUT)
    public void captures_what_is_printed_in_spawned_threads() throws InterruptedException {
        OutputListenerSpy listener = new OutputListenerSpy();

        capturer.captureTo(listener);
        runConcurrently(new Runnable() {
            @Override
            public void run() {
                capturer.out().print("from spawned thread");
            }
        });

        assertThat(listener.out).containsExactly("from spawned thread");
    }

    @Test(timeout = TIMEOUT)
    public void spawned_threads_outliving_the_main_thread_do_WHAT() throws InterruptedException {
        final CountDownLatch beforeFinished = new CountDownLatch(2);
        final CountDownLatch afterFinished = new CountDownLatch(2);
        OutputListenerSpy listener1 = new OutputListenerSpy();
        OutputListenerSpy listener2 = new OutputListenerSpy();

        capturer.captureTo(listener1);
        Thread t = startThread(new Runnable() {
            @Override
            public void run() {
                capturer.out().print("before main finished");
                sync(beforeFinished);
                sync(afterFinished);
                capturer.out().print("after main finished");
            }
        });
        sync(beforeFinished);
        capturer.captureTo(listener2);
        sync(afterFinished);
        t.join();

        assertThat(listener1.out).containsExactly("before main finished", "after main finished");
        assertThat(listener2.out).containsExactly();
    }

    /**
     * PrintStream synchronizes all its operations on itself, but since println() does two calls to the underlying
     * OutputStream (or if the printed text is longer than all the internal buffers), it's possible for stdout and
     * stderr to get interleaved.
     */
    @Test(timeout = TIMEOUT)
    public void printing_to_stdout_and_stderr_concurrently() throws InterruptedException {
        final int ITERATIONS = 30;
        CombinedOutput combinedOutput = new CombinedOutput();
        capturer.captureTo(combinedOutput);

        runConcurrently(
                new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < ITERATIONS; i++) {
                            capturer.out().println("O");
                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < ITERATIONS; i++) {
                            capturer.err().println("E");
                        }
                    }
                }
        );

        assertThat(combinedOutput.toString()).matches("(O\\r?\\n|E\\r?\\n)+");
    }


    // helpers

    private static void runConcurrently(Runnable... commands) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (Runnable command : commands) {
            threads.add(startThread(command));
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private static Thread startThread(Runnable command) {
        Thread t = new Thread(command);
        t.start();
        return t;
    }

    private void sync(CountDownLatch barrier) {
        barrier.countDown();
        try {
            barrier.await(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class OutputListenerSpy implements OutputListener {
        public List<String> out = Collections.synchronizedList(new ArrayList<String>());
        public List<String> err = Collections.synchronizedList(new ArrayList<String>());

        @Override
        public void out(String text) {
            out.add(text);
        }

        @Override
        public void err(String text) {
            err.add(text);
        }
    }

    private static class CombinedOutput implements OutputListener {
        private final StringBuffer sb = new StringBuffer();

        @Override
        public void out(String text) {
            sb.append(text);
        }

        @Override
        public void err(String text) {
            sb.append(text);
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
