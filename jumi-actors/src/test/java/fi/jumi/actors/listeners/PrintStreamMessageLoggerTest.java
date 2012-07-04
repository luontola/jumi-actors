// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.listeners;

import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static fi.jumi.actors.Matchers.containsLineWithWords;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

public class PrintStreamMessageLoggerTest {

    private static final int TIMEOUT = 1000;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PrintStreamMessageLogger logger = new PrintStreamMessageLogger(new PrintStream(output));


    // logging actor messages

    @Test
    public void processing_messages_logs_both_the_actor_and_the_message() {
        logger.onProcessingStarted("actor1", "message1");

        assertThat(output.toString(), containsLineWithWords("actor1 <-", "message1"));
    }

    @Test
    public void sending_messages_outside_an_actor_logs_only_the_message() {
        logger.onMessageSent("message1");

        assertThat(output.toString(), containsLineWithWords("<external> ->", "message1"));
    }

    @Test
    public void sending_messages_from_an_actor_logs_both_the_actor_and_the_message() {
        logger.onProcessingStarted("actor1", "unimportant message");

        logger.onMessageSent("message1");

        assertThat(output.toString(), containsLineWithWords("actor1 ->", "message1"));
    }


    // logging executor commands

    @Test
    public void logs_it_when_an_actors_sends_a_commands_to_an_Executor() {
        Executor realExecutor = mock(Executor.class);
        Executor listenedExecutor = logger.getListenedExecutor(realExecutor);
        logger.onProcessingStarted("actor1", "unimportant message");
        Runnable command = mock(Runnable.class, "DummyCommand");

        listenedExecutor.execute(command);

        assertThat(output.toString(), containsLineWithWords("actor1 ->", command.toString()));
    }

    @Test
    public void logs_it_when_the_Executor_starts_executing_the_command() {
        FakeExecutor realExecutor = new FakeExecutor();
        Executor listenedExecutor = logger.getListenedExecutor(realExecutor);
        logger.onProcessingStarted("actor1", "unimportant message");
        Runnable command = mock(Runnable.class, "DummyCommand");

        listenedExecutor.execute(command);
        realExecutor.processCommands();

        assertThat(output.toString(), containsLineWithWords("FakeExecutor", "<-", command.toString()));
    }

    @Test
    public void the_logged_executor_executes_the_command() {
        FakeExecutor realExecutor = new FakeExecutor();
        Executor listenedExecutor = logger.getListenedExecutor(realExecutor);
        Runnable command = mock(Runnable.class, "DummyCommand");

        listenedExecutor.execute(command);
        realExecutor.processCommands();

        verify(command).run();
    }


    // thread context management

    @Test
    public void the_logged_actor_when_sending_messages_is_a_per_thread_property() throws InterruptedException {
        final CyclicBarrier barrier = new CyclicBarrier(2);

        executeConcurrently(
                new Runnable() {
                    @Override
                    public void run() {
                        logger.onProcessingStarted("actor1", "unimportant message");
                        sync(barrier);
                        logger.onMessageSent("message1");
                        logger.onProcessingFinished();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        logger.onProcessingStarted("actor2", "unimportant message");
                        sync(barrier);
                        logger.onMessageSent("message2");
                        logger.onProcessingFinished();
                    }
                }
        );

        assertThat(output.toString(), containsLineWithWords("actor1 ->", "message1"));
        assertThat(output.toString(), containsLineWithWords("actor2 ->", "message2"));
    }

    /**
     * Makes sure that {@link MessageListener#onProcessingFinished()} is called.
     */
    @Test
    public void the_thread_context_is_cleared_after_the_message_has_been_processed() {
        logger.onProcessingStarted("actor1", "message1");
        logger.onProcessingFinished();

        logger.onMessageSent("message2");

        assertThat(output.toString(), containsLineWithWords("<external> ->", "message2"));
    }

    /**
     * Makes sure that {@link MessageListener#onProcessingFinished()} is called.
     */
    @Test
    public void the_thread_context_is_cleared_after_the_command_has_been_executed() {
        FakeExecutor realExecutor = new FakeExecutor();
        Executor listenedExecutor = logger.getListenedExecutor(realExecutor);
        Runnable command = mock(Runnable.class, "DummyCommand");
        listenedExecutor.execute(command);
        realExecutor.processCommands(); // executes in the current thread

        logger.onMessageSent("message2");

        assertThat(output.toString(), containsLineWithWords("<external> ->", "message2"));
    }


    // additional debug information

    @Test
    public void the_current_thread_is_logged() {
        logger.onMessageSent("message1");

        assertThat(output.toString(), containsString(Thread.currentThread().getName()));
    }

    @Test
    public void unique_id_for_each_message_is_logged() {
        String message = "message1";
        int messageId = System.identityHashCode(message);

        logger.onMessageSent(message);

        String output = this.output.toString();
        assertThat(output, containsString(Integer.toHexString(messageId)));
        assertThat(output).matches("(?s).* 0x[0-9a-f]{8} .*");
    }

    @Test
    public void timestamps_are_shown_as_seconds_since_start_with_microsecond_precision() {
        final Queue<Long> fakeNanoTime = new LinkedList<Long>();
        fakeNanoTime.add(1000000L);
        fakeNanoTime.add(1234567L);
        PrintStreamMessageLogger logger = new PrintStreamMessageLogger(new PrintStream(output)) {
            @Override
            protected long nanoTime() {
                return fakeNanoTime.poll();
            }
        };

        logger.onMessageSent("message1");

        assertThat(output.toString(), containsString("[   0.000235]"));
    }


    // helpers

    private static void executeConcurrently(Runnable... tasks) throws InterruptedException {
        Thread[] threads = new Thread[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            Thread thread = new Thread(tasks[i]);
            thread.start();
            threads[i] = thread;
        }
        for (Thread thread : threads) {
            thread.join(TIMEOUT);
        }
    }

    private static int sync(CyclicBarrier barrier) {
        try {
            return barrier.await(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FakeExecutor implements Executor {

        private final Queue<Runnable> commands = new LinkedList<Runnable>();

        @Override
        public void execute(Runnable command) {
            commands.add(command);
        }

        public void processCommands() {
            Runnable command;
            while ((command = commands.poll()) != null) {
                command.run();
            }
        }
    }
}
