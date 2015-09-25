// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import com.google.common.util.concurrent.AbstractFuture;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class Promise<V> extends AbstractFuture<V> {

    public static <V> Promise<V> of(V value) {
        return new Promise<>();
    }

    public void then(Callback<V> callback) {
    }
}
