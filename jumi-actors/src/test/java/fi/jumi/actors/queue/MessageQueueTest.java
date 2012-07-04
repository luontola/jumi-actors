// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.queue;

import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MessageQueueTest {

    private final MessageQueue<String> messageQueue = new MessageQueue<String>();

    @After
    public void clearThreadInterruptedStatus() {
        Thread.interrupted();
    }

    @Test
    public void send_does_not_change_the_interrupt_status_of_the_current_thread() {
        Thread.currentThread().interrupt();

        messageQueue.send("any message");

        assertThat("interrupt status after send", Thread.currentThread().isInterrupted(), is(true));
    }

    @Test
    public void send_enqueues_even_when_interrupted() {
        Thread.currentThread().interrupt();

        messageQueue.send("the message");

        assertThat(messageQueue.poll(), is("the message"));
    }
}
