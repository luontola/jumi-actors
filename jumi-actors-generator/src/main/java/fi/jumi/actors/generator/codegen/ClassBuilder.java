// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import java.util.*;

@SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
public class ClassBuilder {

    private final String className;
    private final String targetPackage;

    private final StringBuilder methods = new StringBuilder();
    private final List<JavaType> interfaces = new ArrayList<JavaType>();
    private final Imports imports = new Imports();
    private final List<JavaVar> constructorArguments = new ArrayList<JavaVar>();

    public ClassBuilder(String className, String targetPackage) {
        this.className = className;
        this.targetPackage = targetPackage;
    }

    public void implement(JavaType anInterface) {
        interfaces.add(anInterface);
        imports.addImports(anInterface);
    }

    public void fieldsAndConstructorParameters(List<JavaVar> arguments) {
        constructorArguments.addAll(arguments);
    }

    public String getImportedName(JavaType type) {
        imports.addImports(type);
        return type.getSimpleName();
    }

    public void addImports(List<JavaType> imports) {
        imports.addAll(imports);
    }

    public void addPackageImport(String packageName) {
        imports.addPackageImport(packageName);
    }

    public void addMethod(CharSequence methodSource) {
        if (methods.length() > 0) {
            methods.append("\n");
        }
        methods.append(methodSource);
    }

    public GeneratedClass build() {
        String source = packageStatement() + imports + classBody();
        return new GeneratedClass(fileForClass(className), source);
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
            for (JavaVar var : constructorArguments) {
                sb.append("    private final " + var.getType() + " " + var.getName() + ";\n");
            }
            sb.append("\n");
            sb.append("    public " + className + "(" + JavaVar.toFormalArguments(constructorArguments) + ") {\n");
            for (JavaVar var : constructorArguments) {
                sb.append("        this." + var.getName() + " = " + var.getName() + ";\n");
            }
            sb.append("    }\n");
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
}
