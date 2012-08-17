// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

public class BusyWaitBarrier {

    private volatile boolean triggered = false;

    public void trigger() {
        triggered = true;
    }

    public void await() {
        while (!triggered) {
            // busy wait
        }
        triggered = false;
    }
}
