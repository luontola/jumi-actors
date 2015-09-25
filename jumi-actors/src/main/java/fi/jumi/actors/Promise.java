// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import com.google.common.util.concurrent.*;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Future;

@ThreadSafe
public final class Promise<V> extends AbstractFuture<V> {

    public static <V> Deferred<V> defer() {
        return new Deferred<>();
    }

    public static <V> Promise<V> of(@Nullable V value) {
        Deferred<V> deferred = defer();
        deferred.resolve(value);
        return deferred.promise();
    }

    private Promise() {
    }

    public void then(Callback<V> callback) {
        Futures.addCallback(this, new FutureCallback<V>() {
            @Override
            public void onSuccess(@Nullable V result) {
                callback.onResult(result);
            }

            @Override
            public void onFailure(Throwable error) {
                // TODO
            }
        });
    }

    @ThreadSafe
    public static final class Deferred<V> {

        private final Promise<V> promise = new Promise<>();

        private Deferred() {
        }

        public Promise<V> promise() {
            return promise;
        }

        public boolean resolve(@Nullable V result) {
            return promise.set(result);
        }

        public boolean reject(Throwable error) {
            // TODO
            //return promise.setException(error);
            throw new UnsupportedOperationException("TODO");
        }

        public void delegate(Future<V> source) {
            Futures.addCallback(JdkFutureAdapters.listenInPoolThread(source), new FutureCallback<V>() {
                @Override
                public void onSuccess(@Nullable V result) {
                    resolve(result);
                }

                @Override
                public void onFailure(Throwable error) {
                    reject(error);
                }
            });
        }
    }
}
