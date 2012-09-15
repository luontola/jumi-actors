// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import fi.jumi.api.RunVia;
import fi.jumi.test.simpleunit.SimpleUnit;

import java.nio.charset.Charset;

@RunVia(SimpleUnit.class)
@SuppressWarnings({"UnusedDeclaration"})
public class PrintingTest {

    public void testPrintOut() {
        System.out.println("printed to stdout");
    }

    public void testPrintErr() {
        System.err.println("printed to stderr");
    }

    public void testInterleavedPrinting() {
        System.out.print("tr");
        System.err.print("o");
        System.out.print("l");
        System.err.print("o");
        System.out.print("l");
        System.err.print("o");
    }

    public void testPrintNonAscii() {
        System.out.println("default charset is " + Charset.defaultCharset().name());
        System.out.println("åäö");
        System.out.println("你好");
    }
}
