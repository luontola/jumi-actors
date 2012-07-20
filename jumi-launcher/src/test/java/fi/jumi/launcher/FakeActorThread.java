// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.actors.*;

public class FakeActorThread implements ActorThread {

    @Override
    public <T> ActorRef<T> bindActor(Class<T> type, T rawActor) {
        return ActorRef.wrap(rawActor);
    }

    @Override
    public void stop() {
    }
}
