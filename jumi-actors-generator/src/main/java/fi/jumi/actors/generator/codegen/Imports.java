// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import java.util.*;

public class Imports {

    private final List<JavaType> classesToImport = new ArrayList<JavaType>();
    private final List<String> packagesToImport = new ArrayList<String>();

    public String getSimpleName(JavaType type) {
        addImports(type);
        return type.getGenericSimpleName();
    }

    public void addImports(JavaType... types) {
        for (JavaType type : types) {
            classesToImport.addAll(type.getClassImports());
        }
    }

    public void addPackageImport(String packageName) {
        packagesToImport.add(packageName);
    }

    @Override
    public String toString() {
        SortedSet<String> imports = new TreeSet<String>();
        for (JavaType type : classesToImport) {
            if (isAlreadyInScope(type)) {
                continue;
            }
            // TODO: do not import classes from target package
            imports.add(type.getCanonicalName());
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

    private static boolean isAlreadyInScope(JavaType type) {
        return type.getPackage().equals("java.lang");
    }
}
