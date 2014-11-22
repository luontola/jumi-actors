// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

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
}
