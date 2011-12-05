// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator.java;

import java.util.*;

@SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
public class ClassBuilder {

    private final String className;
    private final String targetPackage;

    private final StringBuilder methods = new StringBuilder();
    private final List<JavaType> interfaces = new ArrayList<JavaType>();
    private final List<Class<?>> classesToImport = new ArrayList<Class<?>>();
    private ArgumentList constructorArguments = new ArgumentList();

    public ClassBuilder(String className, String targetPackage) {
        this.className = className;
        this.targetPackage = targetPackage;
    }

    public ClassBuilder implement(JavaType anInterface) {
        interfaces.add(anInterface);
        addImport(anInterface);
        return this;
    }

    public ClassBuilder fieldsAndConstructorParameters(ArgumentList arguments) {
        addImport(arguments);
        this.constructorArguments = arguments;
        return this;
    }

    public String getImportedName(JavaType type) {
        addImport(type);
        return type.getSimpleName();
    }

    public ClassBuilder addImport(ArgumentList arguments) {
        for (Argument argument : arguments) {
            addImport(argument.type);
        }
        return this;
    }

    public ClassBuilder addImport(JavaType... types) {
        for (JavaType type : types) {
            classesToImport.addAll(type.getRawTypesToImport());
        }
        return this;
    }

    public ClassBuilder addMethod(CharSequence methodSource) {
        if (methods.length() > 0) {
            methods.append("\n");
        }
        methods.append(methodSource);
        return this;
    }

    public GeneratedClass build() {
        StringBuilder source = new StringBuilder();
        source.append(packageStatement());
        source.append(importStatements());
        source.append(classBody());
        return new GeneratedClass(fileForClass(className), source.toString());
    }

    private String fileForClass(String className) {
        return targetPackage.replace('.', '/') + "/" + className + ".java";
    }


    // source fragments

    private String packageStatement() {
        return "package " + targetPackage + ";\n\n";
    }

    private StringBuilder importStatements() {
        SortedSet<String> imports = new TreeSet<String>();
        for (Class<?> type : classesToImport) {
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

    private StringBuilder classBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("public class " + className + " implements " + toImplementsDeclaration(interfaces) + " {\n");
        sb.append("\n");
        if (constructorArguments.size() > 0) {
            sb.append(fields(constructorArguments));
            sb.append("\n");
            sb.append(constructor(className, constructorArguments));
            sb.append("\n");
        }
        sb.append(methods);
        sb.append("}\n");
        return sb;
    }

    private static StringBuilder toImplementsDeclaration(List<JavaType> types) {
        StringBuilder sb = new StringBuilder();
        for (JavaType type : types) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.getSimpleName());
        }
        return sb;
    }

    private StringBuilder fields(ArgumentList fields) {
        StringBuilder sb = new StringBuilder();
        for (Argument argument : fields) {
            sb.append("    private final " + argument.type.getSimpleName() + " " + argument.name + ";\n");
        }
        return sb;
    }

    private StringBuilder constructor(String className, ArgumentList fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("    public " + className + "(" + fields.toFormalArguments() + ") {\n");
        for (Argument argument : fields) {
            sb.append("        this." + argument.name + " = " + argument.name + ";\n");
        }
        sb.append("    }\n");
        return sb;
    }
}
