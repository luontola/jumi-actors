// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

import java.lang.reflect.*;
import java.util.*;

public class ArgumentList implements Iterable<Argument> {

    private final List<Argument> arguments = new ArrayList<Argument>();

    public ArgumentList(Method method) {
        Type[] types = method.getGenericParameterTypes();
        for (int i = 0; i < types.length; i++) {
            this.arguments.add(new Argument(JavaType.of(types[i]), "arg" + i));
        }
    }

    public ArgumentList(Argument... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public int size() {
        return arguments.size();
    }

    @Override
    public Iterator<Argument> iterator() {
        return arguments.iterator();
    }

    public StringBuilder toFormalArguments() {
        StringBuilder sb = new StringBuilder();
        for (Argument argument : arguments) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(argument.type.getSimpleName() + " " + argument.name);
        }
        return sb;
    }

    public StringBuilder toActualArguments() {
        StringBuilder sb = new StringBuilder();
        for (Argument argument : arguments) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(argument.name);
        }
        return sb;
    }

    public StringBuilder toToString() {
        StringBuilder sb = new StringBuilder();
        for (Argument argument : arguments) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("\" + " + argument.name + " + \"");
        }
        return sb;
    }
}
