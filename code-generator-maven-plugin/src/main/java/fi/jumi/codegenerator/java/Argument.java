// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

public class Argument {

    public final Type type;
    public final String name;

    public Argument(Type type, String name) {
        this.type = type;
        this.name = name;
    }
}
