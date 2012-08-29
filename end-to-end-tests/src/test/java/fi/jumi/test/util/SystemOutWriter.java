// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import java.io.Writer;

public class SystemOutWriter extends Writer {

    @Override
    public void write(char[] cbuf, int off, int len) {
        System.out.print(new String(cbuf, off, len));
    }

    @Override
    public void flush() {
        System.out.flush();
    }

    @Override
    public void close() {
        flush();
    }
}
