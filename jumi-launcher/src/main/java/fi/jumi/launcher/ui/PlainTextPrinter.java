// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.PrintStream;

@NotThreadSafe
public class PlainTextPrinter implements Printer {

    private final PrintStream out;
    private boolean beginningOfLine = true;

    public PlainTextPrinter(PrintStream out) {
        this.out = out;
    }

    @Override
    public void printOut(String text) {
        printTo(out, text);
    }

    @Override
    public void printErr(String text) {
        printTo(out, text);
    }

    @Override
    public void printlnMeta(String line) {
        if (!beginningOfLine) {
            printTo(out, "\n");
        }
        printTo(out, line);
        printTo(out, "\n");
    }

    private void printTo(PrintStream target, String text) {
        target.print(text);
        beginningOfLine = text.endsWith("\n"); // matches both "\r\n" and "\n"
    }
}
