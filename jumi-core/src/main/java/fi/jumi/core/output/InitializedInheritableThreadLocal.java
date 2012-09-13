// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class InitializedInheritableThreadLocal<T> extends InheritableThreadLocal<T> {

    private final T initialValue;

    public InitializedInheritableThreadLocal(T initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    protected T initialValue() {
        return initialValue;
    }
}
