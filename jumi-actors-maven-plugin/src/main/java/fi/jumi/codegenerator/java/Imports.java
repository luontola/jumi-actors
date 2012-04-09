// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

import java.util.*;

public class Imports {

    private final List<Class<?>> classesToImport = new ArrayList<Class<?>>();
    private final List<String> packagesToImport = new ArrayList<String>();

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

    public void addPackageImport(String packageName) {
        packagesToImport.add(packageName);
    }

    @Override
    public String toString() {
        SortedSet<String> imports = new TreeSet<String>();
        for (Class<?> type : classesToImport) {
            if (type.isPrimitive() || isAlreadyInScope(type)) {
                continue;
            }
            // TODO: do not import classes from target package
            imports.add(type.getName());
        }
        for (String packageName : packagesToImport) {
            imports.add(packageName + ".*");
        }

        StringBuilder sb = new StringBuilder();
        for (String anImport : imports) {
            sb.append("import " + anImport + ";\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private static boolean isAlreadyInScope(Class<?> type) {
        return type.getPackage().getName().equals("java.lang");
    }
}
