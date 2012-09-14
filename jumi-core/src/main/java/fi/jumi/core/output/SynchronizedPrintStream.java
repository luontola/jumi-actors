// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import net.sf.cglib.proxy.*;
import org.apache.commons.io.output.NullOutputStream;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

@ThreadSafe
class SynchronizedPrintStream {

    private static final Factory factory;

    static {
        Enhancer e = new Enhancer();
        e.setSuperclass(PrintStream.class);
        e.setCallback(new SynchronizedMethodInterceptor(null));
        factory = (Factory) e.create(
                new Class[]{OutputStream.class},
                new Object[]{new NullOutputStream()}
        );
    }

    public static PrintStream create(OutputStream out, Charset charset, ReentrantLock lock) {
        return (PrintStream) factory.newInstance(
                new Class[]{OutputStream.class, boolean.class, String.class},
                new Object[]{out, false, charset.name()},
                new Callback[]{new SynchronizedMethodInterceptor(lock)}
        );
    }

    @ThreadSafe
    private static class SynchronizedMethodInterceptor implements MethodInterceptor {
        private final ReentrantLock lock;

        public SynchronizedMethodInterceptor(ReentrantLock lock) {
            this.lock = lock;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            lock.lock();
            try {
                return proxy.invokeSuper(obj, args);
            } finally {
                lock.unlock();
            }
        }
    }
}
