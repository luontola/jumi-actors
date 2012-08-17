// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon.timeout;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public class CommandExecutingTimeout implements Timeout {

    private final Delayer delayedCommand;

    // non-private for testing purposes
    Thread scheduler;

    public CommandExecutingTimeout(Runnable command, long timeout, TimeUnit unit) {
        delayedCommand = new Delayer(command, timeout, unit);
    }

    @Override
    public synchronized void start() {
        if (scheduler != null) {
            throw new IllegalStateException("already started");
        }
        scheduler = new Thread(delayedCommand, "Timeout");
        scheduler.start();
    }

    @Override
    public synchronized void cancel() {
        if (scheduler != null) {
            scheduler.interrupt();
            scheduler = null;
        }
    }

    @ThreadSafe
    private static class Delayer implements Runnable {

        private final Runnable command;
        private final long timeout;
        private final TimeUnit unit;

        public Delayer(Runnable command, long timeout, TimeUnit unit) {
            this.command = command;
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        public void run() {
            try {
                unit.sleep(timeout);
                command.run();
            } catch (InterruptedException e) {
                // timeout cancelled
            }
        }
    }
}
