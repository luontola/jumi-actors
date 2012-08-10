// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon.timeout;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

@ThreadSafe
public class CommandExecutingTimeout implements Timeout {

    // XXX: doesn't work with corePoolSize = 0, so always creates a thread
    // Known bug, fixed in JDK 8(b08), 7u4(b13): http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7091003
    // The applied fix is to always start at least one thread, so there is no way to avoid starting a thread.
    // The design of ScheduledThreadPoolExecutor requires it. See http://stackoverflow.com/a/4361081/62130
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Runnable command;
    private final long timeout;
    private final TimeUnit unit;

    private ScheduledFuture<?> scheduledCommand;

    public CommandExecutingTimeout(Runnable command, long timeout, TimeUnit unit) {
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
