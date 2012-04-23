// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ActorRef<T> {

    private final T proxy;

    public static <T> ActorRef<T> wrap(T proxy) {
        // TODO: we could check that it's really a proxy, and raise and exception otherwise (another ActorRef implementation would then be just for testing)
        return new ActorRef<T>(proxy);
    }

    private ActorRef(T proxy) {
        this.proxy = proxy;
    }

    public T tell() {
        return proxy;
    }
}
