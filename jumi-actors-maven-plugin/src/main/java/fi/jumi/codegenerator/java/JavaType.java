// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

import java.lang.reflect.*;
import java.util.*;

public class JavaType implements Comparable<JavaType> {

    // TODO: replace conditionals with polymorphism

    private final Type type;
    private final JavaType[] typeArguments;

    public JavaType(Type type, JavaType... typeArguments) {
        this.type = type;
        this.typeArguments = typeArguments;
    }

    public JavaType(Type type) {
        this(type, typeArgumentsOf(type));
    }

    private static JavaType[] typeArgumentsOf(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            return asJavaTypes(t.getActualTypeArguments());
        }
        return new JavaType[0];
    }

    private static JavaType[] asJavaTypes(Type[] args) {
        JavaType[] results = new JavaType[args.length];
        for (int i = 0; i < args.length; i++) {
            results[i] = new JavaType(args[i]);
        }
        return results;
    }

    public int compareTo(JavaType that) {
        return getSimpleName().compareTo(that.getSimpleName());
    }

    public String getPackage() {
        if (this.type instanceof Class) {
            Class<?> c = (Class<?>) this.type;
            return c.getPackage().getName();
        }
        if (this.type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) this.type;
            Class<?> c = (Class<?>) t.getRawType();
            return c.getPackage().getName();
        }

        throw new AssertionError("unknown type: " + this.type.getClass() + " of value " + this.type);
    }

    public String getRawName() {
        if (this.type instanceof Class) {
            Class<?> c = (Class<?>) this.type;
            return c.getSimpleName();
        }
        if (this.type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) this.type;
            Class<?> c = (Class<?>) t.getRawType();
            return c.getSimpleName();
        }

        throw new AssertionError("unknown type: " + this.type.getClass() + " of value " + this.type);
    }

    public String getSimpleName() {
        if (this.typeArguments.length > 0) {
            return getRawName() + "<" + typeArgumentsAsString() + ">";
        }
        if (this.type instanceof Class) {
            Class<?> c = (Class<?>) this.type;
            return c.getSimpleName();
        }
        if (this.type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) this.type;
            Class<?> c = (Class<?>) t.getRawType();
            return c.getSimpleName() + "<" + typeArgumentsAsString() + ">";
        }
        if (this.type instanceof WildcardType) {
            // TODO: upper and lower bounds
            return "?";
        }

        throw new AssertionError("unknown type: " + this.type.getClass() + " of value " + this.type);
    }

    private String typeArgumentsAsString() {
        String result = "";
        for (int i = 0; i < typeArguments.length; i++) {
            if (i > 0) {
                result += ", ";
            }
            result += typeArguments[i].getSimpleName();
        }
        return result;
    }

    public List<JavaType> getTypeArguments() {
        return Arrays.asList(typeArguments);
    }

    @Override
    public String toString() {
        if (typeArguments.length > 0) {
            return type.toString() + typeArguments.toString();
        }
        return type.toString();
    }
}
