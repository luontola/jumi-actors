// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent.util;

public class DoNotTransformException extends RuntimeException {

    @Override
    public Throwable fillInStackTrace() {
        // performance optimization: do not generate stack trace
        return this;
    }
}
