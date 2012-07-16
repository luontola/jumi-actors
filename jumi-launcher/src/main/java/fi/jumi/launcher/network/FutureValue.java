// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.network;

import javax.annotation.concurrent.*;
import java.util.concurrent.*;

@ThreadSafe
public class FutureValue<V> extends FutureTask<V> {

    public FutureValue() {
        super(new NullCallable<V>());
    }

    @Override
    public void set(V v) {
        super.set(v);
    }

    @Override
    public void setException(Throwable t) {
        super.setException(t);
    }

    @Immutable
    private static class NullCallable<V> implements Callable<V> {
        @Override
        public V call() throws Exception {
            return null;
        }
    }
}
