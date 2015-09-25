// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import com.google.common.util.concurrent.*;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Future;

@ThreadSafe
public class Promise<V> extends AbstractFuture<V> {

    // TODO: return a mutable handle and keep Promise's public API unmodifiable

    public static <V> Promise<V> pending() {
        return new Promise<>();
    }

    public static <V> Promise<V> of(V value) {
        Promise<V> promise = new Promise<>();
        promise.set(value);
        return promise;
    }

    private Promise() {
    }

    @Override
    public boolean set(@Nullable V value) {
        return super.set(value);
    }

    public void setFrom(Future<V> source) {
        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(source), new FutureCallback<V>() {
            @Override
            public void onSuccess(@Nullable V result) {
                set(result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    public void then(Callback<V> callback) {
        Futures.addCallback(this, new FutureCallback<V>() {
            @Override
            public void onSuccess(@Nullable V result) {
                callback.onResult(result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }
}
