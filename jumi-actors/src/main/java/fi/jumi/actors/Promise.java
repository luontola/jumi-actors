// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

@ThreadSafe
public class Promise<T> implements Future<T> {

    public static <T> Promise<T> of(T value) {
        return new Promise<T>();
    }

    public void then(Callback<T> callback) {
    }


    // TODO: remove us

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
