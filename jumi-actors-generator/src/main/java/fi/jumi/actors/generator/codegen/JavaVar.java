// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import com.google.common.base.Joiner;

import javax.lang.model.element.VariableElement;
import java.util.*;

public class JavaVar {

    private final JavaType type;
    private final String name;

    public static JavaVar of(VariableElement var) {
        JavaType type = JavaType.of(var.asType());
        String name = var.getSimpleName().toString();
        return of(type, name);
    }

    public static JavaVar of(JavaType type, String name) {
        return new JavaVar(type, name);
    }

    private JavaVar(JavaType type, String name) {
        this.type = type;
        this.name = name;
    }

    public JavaType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

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
}
