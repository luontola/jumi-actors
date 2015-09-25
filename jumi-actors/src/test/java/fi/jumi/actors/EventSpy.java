// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EventSpy {

    private final Queue<String> events = new ConcurrentLinkedQueue<>();

    public void log(String event) {
        events.add(event);
    }

    public void await(int expectedCount, long timeout) {
        long limit = System.currentTimeMillis() + timeout;
        while (events.size() < expectedCount) {
            if (System.currentTimeMillis() > limit) {
                throw new AssertionError("timed out; expected " + expectedCount + " or more events but got " + events);
            }
            Thread.yield();
        }
    }

    public void assertContains(String... expected) {
        List<String> actual = new ArrayList<>(events);
        assertThat("events", actual, is(Arrays.asList(expected)));
    }

    public void expectNoMoreEvents() {
        int before = events.size();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        int after = events.size();
        assertThat("expected no more events, but still got some more: " + events, after, is(before));
    }
}
