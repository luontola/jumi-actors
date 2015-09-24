// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import com.google.common.base.Joiner;

import javax.lang.model.element.VariableElement;
import java.util.*;

public abstract class JavaVar {

    public static JavaVar of(VariableElement var) {
        return new AstJavaVar(var); // TODO: unnecessary - extract the type and name
    }

    public static JavaVar of(JavaType type, String name) {
        return new SimpleJavaVar(type, name);
    }

    public abstract JavaType getType();

    public abstract String getName();

    public static String toFormalArguments(ClassBuilder cb, List<JavaVar> arguments) {
        List<String> vars = new ArrayList<String>();
        for (JavaVar var : arguments) {
            vars.add(cb.getSimpleName(var.getType()) + " " + var.getName());
        }
        return Joiner.on(", ").join(vars);
    }

    public static String toActualArguments(List<JavaVar> arguments) {
        List<String> vars = new ArrayList<String>();
        for (JavaVar var : arguments) {
            vars.add(var.getName());
        }
        return Joiner.on(", ").join(vars);
    }

    public static String toActualVarargs(List<JavaVar> arguments) {
        String args = toActualArguments(arguments);
        if (args.length() > 0) {
            return ", " + args;
        }
        return args;
    }


    private static class AstJavaVar extends JavaVar {

        private final VariableElement var;

        private AstJavaVar(VariableElement var) {
            this.var = var;
        }

        @Override
        public JavaType getType() {
            return JavaType.of(var.asType());
        }

        @Override
        public String getName() {
            return var.getSimpleName().toString();
        }
    }

    private static class SimpleJavaVar extends JavaVar {

        private final JavaType type;
        private final String name;

        public SimpleJavaVar(JavaType type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public JavaType getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
