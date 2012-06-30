// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EventSpy {

    private final Queue<String> events = new ConcurrentLinkedQueue<String>();

    public void log(String event) {
        events.add(event);
    }

    public void await(int expectedCount, long timeout) {
        long limit = System.currentTimeMillis() + timeout;
        while (events.size() < expectedCount) {
            if (System.currentTimeMillis() > limit) {
                throw new AssertionError("timed out; received events " + this + " but expected " + expectedCount);
            }
            Thread.yield();
        }
    }

    public void assertContains(String... expected) {
        List<String> actual = new ArrayList<String>(events);
        assertThat("events", actual, is(Arrays.asList(expected)));
    }
}
