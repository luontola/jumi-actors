// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.lang.reflect.*;
import java.lang.reflect.WildcardType;
import java.util.*;

public abstract class JavaType {

    // TODO: use javax.lang.model.util.Types

    public static JavaType of(TypeElement type) {
        return of(type, null, null);
    }

    public static JavaType of(TypeMirror type) {
        TypeKind kind = type.getKind();
        if (kind == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) type;
            TypeElement element = (TypeElement) dt.asElement(); // XXX: strips away generics
            return JavaType.of(element, type, null);
        }
        throw new IllegalArgumentException("unsupported kind " + kind);
    }

    public static JavaType of(Type type) {
        return of(null, null, type, typeArgumentsOf(type));
    }

    public static JavaType of(Type type, JavaType... typeArguments) {
        return of(null, null, type, typeArguments);
    }

    private static JavaType of(TypeElement typeElement, TypeMirror typeMirror, Type type, JavaType... typeArguments) {
        if (typeElement != null) {
            if (typeMirror == null) {
                typeMirror = typeElement.asType();
            }
            return new AstJavaType(typeElement, typeMirror);
        }
        if (type instanceof Class && typeArguments.length == 0) {
            return new RawJavaType((Class<?>) type);
        }
        if (type instanceof Class && typeArguments.length > 0) {
            return new GenericJavaType(JavaType.of(type), typeArguments);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) t.getRawType();
            return new GenericJavaType(JavaType.of(rawType), typeArguments);
        }
        if (type instanceof WildcardType) {
            return new WildcardJavaType();
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

    public abstract String getCanonicalName();

    public abstract String getRawSimpleName();

    // The following operations should be used only through the Imports class:

    protected abstract String getPackage();

    protected abstract String getGenericSimpleName();

    protected abstract List<JavaType> getClassImports();


    private static class RawJavaType extends JavaType {
        private final Class<?> type;

        private RawJavaType(Class<?> type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + type + ")";
        }

        @Override
        public String getPackage() {
            return type.getPackage().getName();
        }

        @Override
        public String getCanonicalName() {
            return type.getCanonicalName();
        }

        @Override
        public String getRawSimpleName() {
            return type.getSimpleName();
        }

        @Override
        public String getGenericSimpleName() {
            return type.getSimpleName();
        }

        @Override
        public List<JavaType> getClassImports() {
            List<JavaType> imports = new ArrayList<>();
            if (!type.isPrimitive()) {
                imports.add(this);
            }
            return imports;
        }
    }

    private static class GenericJavaType extends JavaType {
        private final JavaType type;
        private final JavaType[] typeArguments;

        private GenericJavaType(JavaType type, JavaType[] typeArguments) {
            this.type = type;
            this.typeArguments = typeArguments;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + type + ", " + Arrays.toString(typeArguments) + ")";
        }

        @Override
        public String getPackage() {
            return type.getPackage();
        }

        @Override
        public String getCanonicalName() {
            return type.getCanonicalName();
        }

        @Override
        public String getRawSimpleName() {
            return type.getRawSimpleName();
        }

        @Override
        public String getGenericSimpleName() {
            return type.getGenericSimpleName() + "<" + typeArgumentsAsString() + ">";
        }

        private String typeArgumentsAsString() {
            String result = "";
            for (int i = 0; i < typeArguments.length; i++) {
                if (i > 0) {
                    result += ", ";
                }
                result += typeArguments[i].getGenericSimpleName();
            }
            return result;
        }

        @Override
        public List<JavaType> getClassImports() {
            List<JavaType> imports = new ArrayList<>();
            imports.add(type);
            for (JavaType typeArgument : typeArguments) {
                imports.addAll(typeArgument.getClassImports());
            }
            return imports;
        }

    }

    private static class WildcardJavaType extends JavaType {

        private WildcardJavaType() {
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + getGenericSimpleName() + ")";
        }

        @Override
        public String getPackage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCanonicalName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRawSimpleName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getGenericSimpleName() {
            // TODO: upper and lower bounds
            return "?";
        }

        @Override
        public List<JavaType> getClassImports() {
            return Collections.emptyList();
        }

    }

    private static class AstJavaType extends JavaType {

        // XXX: is there some way for just the element or type to be enough?
        private final TypeElement element;
        private final DeclaredType type;
        private final List<JavaType> typeArguments = new ArrayList<>();

        public AstJavaType(TypeElement element, TypeMirror type) {
            if (type.getKind() != TypeKind.DECLARED) {
                throw new IllegalArgumentException("type " + type + " had kind " + type.getKind());
            }
            this.element = element;
            this.type = (DeclaredType) type;
            for (TypeMirror typeArgument : this.type.getTypeArguments()) {
                this.typeArguments.add(JavaType.of(typeArgument));
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + element + ", " + type + ", " + typeArguments + ")";
        }

        @Override
        public String getPackage() {
            Element e = element;
            while (e.getKind() != ElementKind.PACKAGE) {
                e = e.getEnclosingElement();
            }
            return e.toString();
        }

        @Override
        public String getCanonicalName() {
            return element.getQualifiedName().toString();
        }

        @Override
        public String getRawSimpleName() {
            return element.getSimpleName().toString();
        }

        @Override
        public String getGenericSimpleName() {
            StringBuilder sb = new StringBuilder();
            sb.append(type.asElement().getSimpleName());
            if (!typeArguments.isEmpty()) {
                sb.append("<");
                for (JavaType typeArgument : typeArguments) {
                    sb.append(typeArgument.getGenericSimpleName());
                }
                sb.append(">");
            }
            return sb.toString();
        }

        @Override
        public List<JavaType> getClassImports() {
            ArrayList<JavaType> imports = new ArrayList<>();
            imports.add(this);
            for (JavaType typeArgument : typeArguments) {
                imports.addAll(typeArgument.getClassImports());
            }
            return imports;
        }

    }
}
