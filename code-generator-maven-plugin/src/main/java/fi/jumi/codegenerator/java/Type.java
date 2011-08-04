// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

public class Type implements Comparable<Type> {

    private final String name;

    public Type(Class<?> clazz) {
        this(clazz.getName());
    }

    public Type(String name) {
        this.name = name;
    }

    public int compareTo(Type that) {
        return this.name.compareTo(that.name);
    }

    public String getPackage() {
        return name.substring(0, name.lastIndexOf('.'));
    }

    public String getSimpleName() {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public String toString() {
        return name;
    }
}
