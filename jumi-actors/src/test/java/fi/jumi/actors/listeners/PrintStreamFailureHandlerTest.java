// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.listeners;

import fi.jumi.actors.DummyException;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class PrintStreamFailureHandlerTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PrintStreamFailureHandler failureHandler = new PrintStreamFailureHandler(new PrintStream(output));

    @Test
    public void logs_uncaught_exceptions() {
        failureHandler.uncaughtException("the actor", "the message", new DummyException());

        String output = this.output.toString();
        assertThat(output, containsString("uncaught exception"));
        assertThat(output, containsString("the actor"));
        assertThat(output, containsString("the message"));
        assertThat(output, containsString(DummyException.class.getName()));
        assertThat("should contain the stack trace", output, containsString("at " + getClass().getName()));
    }
}
