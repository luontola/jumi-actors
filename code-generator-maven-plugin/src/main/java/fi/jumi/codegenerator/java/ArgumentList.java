// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

import java.lang.reflect.Method;
import java.util.*;

public class ArgumentList {

    private final List<Parameter> parameters = new ArrayList<Parameter>();

    public ArgumentList(Method method) {
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            parameters.add(new Parameter(new Type(types[i]), "arg" + i));
        }
    }

    public StringBuilder toFormalArguments() {
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(parameter.type.getSimpleName() + " " + parameter.name);
        }
        return sb;
    }

    public StringBuilder toActualArguments() {
        StringBuilder sb = new StringBuilder();
        for (Parameter parameter : parameters) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(parameter.name);
        }
        return sb;
    }
}
