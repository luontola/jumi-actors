// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class RingTest {

    private final MessageListener messageListener = mock(MessageListener.class);
    private final SingleThreadedActors actors = new SingleThreadedActors(
            new DynamicEventizerProvider(),
            new CrashEarlyFailureHandler(),
            messageListener
    );
    private final ActorThread actorThread = actors.startActorThread();


    @Test
    public void one_node_one_round_trip() {
        int ringSize = 1;
        int roundTrips = 1;
        checkRingContract(ringSize, roundTrips);
    }

    @Test
    public void many_nodes_one_round_trip() {
        int ringSize = 10;
        int roundTrips = 1;
        checkRingContract(ringSize, roundTrips);
    }

    @Test
    public void one_node_many_round_trips() {
        int ringSize = 1;
        int roundTrips = 10;
        checkRingContract(ringSize, roundTrips);
    }

    @Test
    public void many_nodes_many_round_trips() {
        int ringSize = 3;
        int roundTrips = 5;
        checkRingContract(ringSize, roundTrips);
    }

    private void checkRingContract(int ringSize, int roundTrips) {
        ActorRef<Ring> ring = createRing(ringSize);

        doRoundTrips(ring, roundTrips);

        verifyNumberOfMessagesSent(ringSize, roundTrips);
    }

    private ActorRef<Ring> createRing(int ringSize) {
        ActorRef<Ring> first = actorThread.bindActor(Ring.class, new RingStart(actorThread));
        first.tell().build(ringSize, first);
        actors.processEventsUntilIdle();
        return first;
    }

    private void doRoundTrips(ActorRef<Ring> ring, int roundTrips) {
        reset(messageListener);
        ring.tell().forward(roundTrips);
        actors.processEventsUntilIdle();
    }

    private void verifyNumberOfMessagesSent(int ringSize, int roundTrips) {
        int initialMessage = 1;
        verify(messageListener, times(ringSize * roundTrips + initialMessage)).onProcessingStarted(any(), any());
    }
}
