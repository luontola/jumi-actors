// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.listeners;

import javax.annotation.concurrent.Immutable;
import java.io.PrintStream;

@Immutable
public class PrintStreamFailureHandler implements FailureHandler {

    private final PrintStream out;

    public PrintStreamFailureHandler(PrintStream out) {
        this.out = out;
    }

    @Override
    public void uncaughtException(Object actor, Object message, Throwable exception) {
        synchronized (out) {
            out.println("uncaught exception from " + actor + " when processing " + message);
            exception.printStackTrace(out);
        }
    }
}
