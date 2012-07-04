// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.logging;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;
import java.util.Locale;
import java.util.concurrent.Executor;

@ThreadSafe
public class PrintStreamMessageLogger implements MessageListener {

    private static final String OUTGOING_MESSAGE = "->";
    private static final String INCOMING_MESSAGE = "<-";

    private final PrintStream out;
    private final ThreadLocal<Object> currentActor = new ThreadLocal<Object>();
    private final long startTime;

    public PrintStreamMessageLogger(PrintStream out) {
        this.out = out;
        this.startTime = nanoTime();
    }

    @Override
    public void onMessageSent(Object message) {
        logMessage(OUTGOING_MESSAGE, message);
    }

    @Override
    public void onProcessingStarted(Object actor, Object message) {
        currentActor.set(actor);
        logMessage(INCOMING_MESSAGE, message);
    }

    private void logMessage(String messageDirection, Object message) {
        String threadName = Thread.currentThread().getName();
        int messageId = System.identityHashCode(message);
        out.println(String.format(Locale.ENGLISH, "[%11.6f] [%s] %s %s 0x%08x %s",
                secondsSinceStart(), threadName, currentActorFormatted(), messageDirection, messageId, message));
    }

    private double secondsSinceStart() {
        long nanosSinceStart = nanoTime() - startTime;
        return nanosSinceStart / 1000000000.0;
    }

    protected long nanoTime() { // protected to allow overriding in tests
        return System.nanoTime();
    }

    private String currentActorFormatted() {
        Object currentActor = this.currentActor.get();
        if (currentActor == null) {
            return "<external>";
        }
        return currentActor.toString();
    }

    @Override
    public void onProcessingFinished() {
        currentActor.remove();
    }

    @Override
    public Executor getListenedExecutor(Executor realExecutor) {
        return new LoggedExecutor(realExecutor);
    }


    @ThreadSafe
    private class LoggedExecutor implements Executor {
        private final Executor realExecutor;

        public LoggedExecutor(Executor realExecutor) {
            this.realExecutor = realExecutor;
        }

        @Override
        public void execute(Runnable realCommand) {
            onMessageSent(realCommand);
            realExecutor.execute(new LoggedRunnable(realExecutor, realCommand));
        }
    }

    @ThreadSafe
    private class LoggedRunnable implements Runnable {
        private final Executor realExecutor;
        private final Runnable realCommand;

        public LoggedRunnable(Executor realExecutor, Runnable realCommand) {
            this.realExecutor = realExecutor;
            this.realCommand = realCommand;
        }

        @Override
        public void run() {
            onProcessingStarted(realExecutor, realCommand);
            try {
                realCommand.run();
            } finally {
                onProcessingFinished();
            }
        }
    }
}
