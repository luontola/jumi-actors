// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
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
    private final Imports imports = new Imports();
    private ArgumentList constructorArguments = new ArgumentList();

    public ClassBuilder(String className, String targetPackage) {
        this.className = className;
        this.targetPackage = targetPackage;
    }

    public ClassBuilder implement(JavaType anInterface) {
        interfaces.add(anInterface);
        imports.addImports(anInterface);
        return this;
    }

    public ClassBuilder fieldsAndConstructorParameters(ArgumentList arguments) {
        addImports(arguments);
        this.constructorArguments = arguments;
        return this;
    }

    public String getImportedName(JavaType type) {
        imports.addImports(type);
        return type.getSimpleName();
    }

    public ClassBuilder addImports(ArgumentList arguments) {
        imports.addImports(arguments);
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
        source.append(imports.importStatements());
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
