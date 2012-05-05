// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.logging;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;

@ThreadSafe
public class PrintStreamMessageLogger implements MessageLogger {

    private final PrintStream out;
    private final ThreadLocal<Object> currentActor = new ThreadLocal<Object>();

    public PrintStreamMessageLogger(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onMessageSent(Object message) {
        out.println(currentActorFormatted() + " -> " + message);
    }

    private String currentActorFormatted() {
        Object currentActor = this.currentActor.get();
        if (currentActor == null) {
            return "<external>";
        }
        return currentActor.toString();
    }

    @Override
    public void onProcessingStarted(Object actor, Object message) {
        currentActor.set(actor);
        out.println(currentActorFormatted() + " <- " + message);
    }

    @Override
    public void onProcessingFinished() {
        currentActor.remove();
    }
}
