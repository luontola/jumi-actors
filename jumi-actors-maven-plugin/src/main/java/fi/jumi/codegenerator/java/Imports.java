// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

import java.util.*;

public class Imports {

    private final List<Class<?>> classesToImport = new ArrayList<Class<?>>();

    public void addImports(ArgumentList arguments) {
        for (Argument argument : arguments) {
            addImports(argument.type);
        }
    }

    public void addImports(JavaType... types) {
        for (JavaType type : types) {
            classesToImport.addAll(type.getRawTypesToImport());
        }
    }

    public StringBuilder importStatements() {
        SortedSet<String> imports = new TreeSet<String>();
        for (Class<?> type : classesToImport) {
            // FIXME: primitive types
            String packageName = type.getPackage().getName();
            // TODO: do not import classes from target package
            if (packageName.equals("java.lang")) {
                continue;
            }
            imports.add(type.getName());
        }

        StringBuilder sb = new StringBuilder();
        for (String anImport : imports) {
            sb.append("import " + anImport + ";\n");
        }
        sb.append("\n");
        return sb;
    }
}
