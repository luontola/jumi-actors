// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

public class SneakyThrow {

    // See http://www.jayway.com/2010/01/29/sneaky-throw/

    public static RuntimeException rethrow(Throwable t) {
        SneakyThrow.<RuntimeException>rethrow0(t);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrow0(Throwable t) throws T {
        throw (T) t;
    }
}
