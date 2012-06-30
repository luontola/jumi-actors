// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.mq;

public interface MessageReceiver<T> {

    T take() throws InterruptedException;

    T poll();
}
