// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import fi.jumi.actors.ActorRef;

public interface Ring {

    void build(int ringSize, ActorRef<Ring> first);

    void forward(int roundTrips);
}
