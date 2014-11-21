// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

public class Argument {

    public final JavaType type;
    public final String name;

    public Argument(JavaType type, String name) {
        this.type = type;
        this.name = name;
    }
}
