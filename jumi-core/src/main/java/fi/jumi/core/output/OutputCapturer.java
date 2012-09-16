// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import org.apache.commons.io.output.WriterOutputStream;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

@ThreadSafe
public class OutputCapturer {

    // PrintStream does not do any buffering of its own; only the OutputStream that it delegates to may buffer.

    private final OutCapturer outCapturer = new OutCapturer();
    private final ErrCapturer errCapturer = new ErrCapturer();
    private final PrintStream out;
    private final PrintStream err;

    public OutputCapturer(OutputStream realOut, OutputStream realErr, Charset charset) {
        OutputStream capturedOut = new WriterOutputStream(outCapturer, charset);
        OutputStream capturedErr = new WriterOutputStream(errCapturer, charset);
        ReentrantLock lock = new ReentrantLock();
        out = SynchronizedPrintStream.create(new OutputStreamReplicator(realOut, capturedOut), charset, lock);
        err = SynchronizedPrintStream.create(new OutputStreamReplicator(realErr, capturedErr), charset, lock);
    }

    public PrintStream out() {
        return out;
    }

    public PrintStream err() {
        return err;
    }

    public void captureTo(OutputListener listener) {
        outCapturer.setListener(listener);
        errCapturer.setListener(listener);
    }


    @ThreadSafe
    private static class OutCapturer extends AbstractCapturer {
        @Override
        public void write(String text) {
            listener.get().out(text);
        }
    }

    @ThreadSafe
    private static class ErrCapturer extends AbstractCapturer {
        @Override
        public void write(String text) {
            listener.get().err(text);
        }
    }

    @ThreadSafe
    private static abstract class AbstractCapturer extends Writer {
        protected final ThreadLocal<OutputListener> listener = new InitializedInheritableThreadLocal<OutputListener>(new NullOutputListener());

        public void setListener(OutputListener listener) {
            this.listener.set(listener);
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            write(new String(cbuf, off, len));
        }

        @Override
        public abstract void write(String text);

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }

    @ThreadSafe
    private static class OutputStreamReplicator extends OutputStream {
        private final OutputStream out1;
        private final OutputStream out2;

        public OutputStreamReplicator(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            out1.write(bytes, off, len);
            out1.flush();
            out2.write(bytes, off, len);
            out2.flush();
        }

        @Override
        public void write(int b) throws IOException {
            out1.write(b);
            out1.flush();
            out2.write(b);
            out2.flush();
        }
    }
}
