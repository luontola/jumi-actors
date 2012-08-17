// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import fi.jumi.actors.*;

public class RingStart extends RingNode {

    public RingStart(ActorThread actorThread) {
        super(actorThread);
    }

    @Override
    public void forward(int roundTrips) {
        if (roundTrips > 0) {
            roundTrips--;
            super.forward(roundTrips);
        }
    }
}

class RingNode implements Ring {

    private final ActorThread actorThread;
    private ActorRef<Ring> next;

    public RingNode(ActorThread actorThread) {
        this.actorThread = actorThread;
    }

    @Override
    public void build(int ringSize, ActorRef<Ring> first) {
        ringSize--;
        if (ringSize > 0) {
            next = actorThread.bindActor(Ring.class, new RingNode(actorThread));
            next.tell().build(ringSize, first);
        } else {
            next = first;
        }
    }

    @Override
    public void forward(int roundTrips) {
        next.tell().forward(roundTrips);
    }
}
