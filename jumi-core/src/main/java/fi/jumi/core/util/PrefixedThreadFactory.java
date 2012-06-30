// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
public class PrefixedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger nextSuffix = new AtomicInteger(1);

    public PrefixedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + nextSuffix.getAndIncrement();
        return new Thread(r, name);
    }
}
