// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon.timeout;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

@ThreadSafe
public class RunnableExecutingTimeout implements Timeout {

    // FIXME: doesn't work with corePoolSize = 0
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Runnable command;
    private final long timeout;
    private final TimeUnit unit;

    private ScheduledFuture<?> scheduledCommand;

    public RunnableExecutingTimeout(Runnable command, long timeout, TimeUnit unit) {
        this.command = command;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public synchronized void start() {
        scheduledCommand = scheduler.schedule(command, timeout, unit);
    }

    @Override
    public synchronized void cancel() {
        scheduledCommand.cancel(false);
    }
}
