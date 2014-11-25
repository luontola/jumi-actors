// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.lang.reflect.*;
import java.lang.reflect.WildcardType;
import java.util.*;

public abstract class JavaType {

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

    public abstract String getPackage();

    public abstract String getName();

    public abstract String getRawName();

    public abstract String getSimpleName();

    public abstract List<JavaType> getClassImports();

    public abstract List<JavaMethod> getMethods();


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
        public String getName() {
            return type.getName();
        }

        @Override
        public String getRawName() {
            return type.getSimpleName();
        }

        @Override
        public String getSimpleName() {
            return type.getSimpleName();
        }

        @Override
        public List<JavaType> getClassImports() {
            List<JavaType> imports = new ArrayList<JavaType>();
            if (!type.isPrimitive()) {
                imports.add(this);
            }
            return imports;
        }

        @Override
        public List<JavaMethod> getMethods() {
            return Collections.emptyList();
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
        public String getName() {
            return type.getName();
        }

        @Override
        public String getRawName() {
            return type.getRawName();
        }

        @Override
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

        @Override
        public List<JavaType> getClassImports() {
            List<JavaType> imports = new ArrayList<JavaType>();
            imports.add(type);
            for (JavaType typeArgument : typeArguments) {
                imports.addAll(typeArgument.getClassImports());
            }
            return imports;
        }

        @Override
        public List<JavaMethod> getMethods() {
            return Collections.emptyList();
        }
    }

    private static class WildcardJavaType extends JavaType {

        private WildcardJavaType() {
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + getSimpleName() + ")";
        }

        @Override
        public String getPackage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return getSimpleName();
        }

        @Override
        public String getRawName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSimpleName() {
            // TODO: upper and lower bounds
            return "?";
        }

        @Override
        public List<JavaType> getClassImports() {
            return Collections.emptyList();
        }

        @Override
        public List<JavaMethod> getMethods() {
            return Collections.emptyList();
        }
    }

    private static class AstJavaType extends JavaType {

        // XXX: is there some way for just the element or type to be enough?
        private final TypeElement element;
        private final DeclaredType type;
        private final List<JavaType> typeArguments = new ArrayList<JavaType>();

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
        public String getName() {
            return element.toString();
        }

        @Override
        public String getRawName() {
            return element.getSimpleName().toString();
        }

        @Override
        public String getSimpleName() {
            StringBuilder sb = new StringBuilder();
            sb.append(type.asElement().getSimpleName());
            if (!typeArguments.isEmpty()) {
                sb.append("<");
                for (JavaType typeArgument : typeArguments) {
                    sb.append(typeArgument.getSimpleName());
                }
                sb.append(">");
            }
            return sb.toString();
        }

        @Override
        public List<JavaType> getClassImports() {
            ArrayList<JavaType> imports = new ArrayList<JavaType>();
            imports.add(this);
            imports.addAll(typeArguments);
            return imports;
        }

        @Override
        public List<JavaMethod> getMethods() {
            ArrayList<JavaMethod> methods = new ArrayList<JavaMethod>();
            for (Element element : this.element.getEnclosedElements()) {
                if (element.getKind() == ElementKind.METHOD) {
                    methods.add(new JavaMethod((ExecutableElement) element));
                }
            }
            // TODO: get methods of parent interfaces
            return methods;
        }
    }
}
