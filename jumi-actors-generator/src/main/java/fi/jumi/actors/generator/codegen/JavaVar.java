// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import com.google.common.base.Joiner;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;

public class JavaVar {

    private final VariableElement var;

    public JavaVar(VariableElement var) {
        this.var = var;
    }

    public String getName() {
        return var.getSimpleName().toString();
    }

    public TypeMirror getType() {
        return var.asType();
    }

    public static String toFormalArguments(List<JavaVar> arguments) {
        List<String> vars = new ArrayList<String>();
        for (JavaVar var : arguments) {
            vars.add(var.getType() + " " + var.getName());
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
