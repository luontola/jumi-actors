// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.logging;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;

@ThreadSafe
public class PrintStreamMessageLogger implements MessageLogger {

    private static final String OUTGOING_MESSAGE = "->";
    private static final String INCOMING_MESSAGE = "<-";

    private final PrintStream out;
    private final ThreadLocal<Object> currentActor = new ThreadLocal<Object>();

    public PrintStreamMessageLogger(PrintStream out) {
        this.out = out;
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
        out.println(String.format("[%s] %s %s 0x%08x %s",
                threadName, currentActorFormatted(), messageDirection, messageId, message));
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
}
