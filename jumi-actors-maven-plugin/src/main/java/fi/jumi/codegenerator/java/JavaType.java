// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

import java.lang.reflect.*;
import java.util.*;

public abstract class JavaType implements Comparable<JavaType> {

    public static JavaType of(Type type) {
        return JavaType.of(type, typeArgumentsOf(type));
    }

    public static JavaType of(Type type, JavaType... typeArguments) {
        if (type instanceof Class && typeArguments.length == 0) {
            return new RawType((Class<?>) type);
        }
        if (type instanceof Class && typeArguments.length > 0) {
            return new GenericType((Class<?>) type, typeArguments);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) t.getRawType();
            return new GenericType(rawType, typeArguments);
        }
        if (type instanceof WildcardType) {
            return new GenericWildcardType();
        }
        throw new IllegalArgumentException("unsupported type " + type);
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
            results[i] = JavaType.of(args[i]);
        }
        return results;
    }

    public int compareTo(JavaType that) {
        return getSimpleName().compareTo(that.getSimpleName());
    }

    public abstract String getPackage();

    public abstract String getRawName();

    public abstract String getSimpleName();

    public abstract Collection<? extends JavaType> getRawTypesToImport();


    private static class RawType extends JavaType {
        private final Class<?> type;

        private RawType(Class<?> type) {
            super();
            this.type = type;
        }

        public String getPackage() {
            return type.getPackage().getName();
        }

        public String getRawName() {
            return type.getSimpleName();
        }

        public String getSimpleName() {
            return type.getSimpleName();
        }

        public Collection<? extends JavaType> getRawTypesToImport() {
            return Arrays.asList(this);
        }
    }

    private static class GenericType extends JavaType {
        private final Class<?> type;
        private final JavaType[] typeArguments;

        private GenericType(Class<?> aClass, JavaType[] typeArguments) {
            super();
            this.type = aClass;
            this.typeArguments = typeArguments;
        }

        public String getPackage() {
            return type.getPackage().getName();
        }

        public String getRawName() {
            return type.getSimpleName();
        }

        public String getSimpleName() {
            return type.getSimpleName() + "<" + typeArgumentsAsString() + ">";
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

        public Collection<? extends JavaType> getRawTypesToImport() {
            ArrayList<JavaType> imports = new ArrayList<JavaType>();
            imports.add(this);
            Collections.addAll(imports, typeArguments);
            return imports;
        }
    }

    private static class GenericWildcardType extends JavaType {

        private GenericWildcardType() {
            super();
        }

        public String getPackage() {
            throw new UnsupportedOperationException();
        }

        public String getRawName() {
            throw new UnsupportedOperationException();
        }

        public String getSimpleName() {
            // TODO: upper and lower bounds
            return "?";
        }

        public Collection<? extends JavaType> getRawTypesToImport() {
            return Collections.emptyList();
        }
    }
}
