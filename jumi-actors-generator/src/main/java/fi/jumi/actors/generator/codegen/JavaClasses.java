// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import javax.lang.model.element.*;
import java.util.*;

public class JavaClasses {

    public static List<JavaMethod> getMethods(TypeElement clazz) {
        ArrayList<JavaMethod> methods = new ArrayList<JavaMethod>();
        for (Element enclosedElement : clazz.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                methods.add(new JavaMethod((ExecutableElement) enclosedElement));
            }
        }
        // TODO: get methods of parent interfaces
        return methods;
    }
}
