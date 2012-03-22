// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

@Immutable
public class RunId implements Serializable {

    private static final int FIRST_ID = 1;

    private final int id;

    public RunId(int id) {
        if (id < FIRST_ID) {
            throw new IllegalArgumentException("id must be " + FIRST_ID + " or greater, but was: " + id);
        }
        this.id = id;
    }

    public int toInt() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RunId)) {
            return false;
        }
        RunId that = (RunId) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "RunId(" + id + ")";
    }
}
