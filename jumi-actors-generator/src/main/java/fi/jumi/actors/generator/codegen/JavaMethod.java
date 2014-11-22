// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import javax.lang.model.element.*;
import java.util.*;

public class JavaMethod {

    private final ExecutableElement element;

    public JavaMethod(ExecutableElement element) {
        this.element = element;
    }

    public List<JavaType> getClassImports() {
        return Collections.emptyList(); // TODO
    }

    public String getName() {
        return element.getSimpleName().toString();
    }

    public List<JavaVar> getArguments() {
        ArrayList<JavaVar> vars = new ArrayList<JavaVar>();
        for (VariableElement var : element.getParameters()) {
            vars.add(new JavaVar(var));
        }
        return vars;
    }
}
