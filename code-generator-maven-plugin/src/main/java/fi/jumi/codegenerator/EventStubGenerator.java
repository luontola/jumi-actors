// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import fi.jumi.codegenerator.java.*;
import org.codehaus.plexus.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
public class EventStubGenerator {

    private Class<?> listenerType;
    private String targetPackage;
    private Type listenerInterface;
    private Type eventInterface;
    private Type factoryInterface;
    private Type senderInterface;

    // configuration

    public void setListenerType(Class<?> listenerType) {
        this.listenerType = listenerType;
        this.listenerInterface = new Type(listenerType);
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public void setEventInterface(String eventInterface) {
        this.eventInterface = new Type(eventInterface);
    }

    public void setFactoryInterface(String factoryInterface) {
        this.factoryInterface = new Type(factoryInterface);
    }

    public void setSenderInterface(String senderInterface) {
        this.senderInterface = new Type(senderInterface);
    }

    // public API

    public String getFactoryPath() {
        return fileForClass(factoryName());
    }

    public String getFactorySource() {
        StringBuilder sb = new StringBuilder();
        sb.append(packageStatement());
        sb.append(importStatements());

        // TODO: class(name, interface)?
        sb.append("public class " + factoryName() + " implements " + factoryInterfaceName() + " {\n");
        sb.append("\n");

        sb.append("    public Class<" + listenerName() + "> getType() {\n");
        sb.append("        return " + listenerName() + ".class;\n");
        sb.append("    }\n");

        sb.append("\n");

        sb.append("    public " + listenerName() + " newFrontend(" + senderName() + " target) {\n");
        sb.append("        return new " + frontendName() + "(target);\n");
        sb.append("    }\n");

        sb.append("\n");

        // TODO: method(name, args, body)?
        // TODO: returnNewInstance(classname, args)?
        sb.append("    public " + senderName() + " newBackend(" + listenerName() + " target) {\n");
        sb.append("        return new " + backendName() + "(target);\n");
        sb.append("    }\n");

        sb.append("}\n");
        return sb.toString();
    }

    public String getFrontendPath() {
        return fileForClass(frontendName());
    }

    public String getFrontendSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(packageStatement());
        sb.append(importStatements());

        sb.append("public class " + frontendName() + " implements " + listenerName() + " {\n");
        sb.append("\n");

        sb.append("    private final " + senderName() + " sender;\n");
        sb.append("\n");

        // TODO: constructor(classname, args)? fields based on constructor?
        sb.append("    public " + frontendName() + "(" + senderName() + " sender) {\n");
        sb.append("        this.sender = sender;\n");
        sb.append("    }\n");

        sb.append("\n");

        for (Method method : listenerType.getMethods()) {
            sb.append(methodCallToEventDelegator(method));
        }

        sb.append("}\n");
        return sb.toString();
    }

    private StringBuilder methodCallToEventDelegator(Method method) {
        ArgumentList arguments = new ArgumentList(method);
        StringBuilder sb = new StringBuilder();
        sb.append("    public void " + method.getName() + "(" + arguments.toFormalArguments() + ") {\n");
        sb.append("        sender.send(new " + eventWrapperName(method) + "(" + arguments.toActualArguments() + "));\n");
        sb.append("    }\n");
        return sb;
    }

    public String getBackendPath() {
        return fileForClass(backendName());
    }

    public String getBackendSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(packageStatement());
        sb.append(importStatements());

        sb.append("public class " + backendName() + " implements " + senderName() + " {\n");
        sb.append("\n");

        String arg0type = listenerName();
        String arg0name = "listener";

        sb.append("    private final " + arg0type + " " + arg0name + ";\n");
        sb.append("\n");

        // TODO: constructor(classname, args)? fields based on constructor?
        sb.append("    public " + backendName() + "(" + arg0type + " " + arg0name + ") {\n");
        sb.append("        this." + arg0name + " = " + arg0name + ";\n");
        sb.append("    }\n");

        sb.append("\n");

        sb.append("    public void send(" + eventName() + " message) {\n");
        sb.append("        message.fireOn(listener);\n");
        sb.append("    }\n");

        sb.append("}\n");

        return sb.toString();
    }


    // source fragments

    private String packageStatement() {
        return "package " + targetPackage + ";\n\n";
    }

    private StringBuilder importStatements() {
        StringBuilder sb = new StringBuilder();
        for (String classToImport : classesToImport()) {
            sb.append("import " + classToImport + ";\n");
        }
        sb.append("\n");
        return sb;
    }

    private Collection<String> classesToImport() {
        SortedSet<Type> singleClassImports = new TreeSet<Type>();
        singleClassImports.add(listenerInterface);
        singleClassImports.add(eventInterface);
        singleClassImports.add(factoryInterface);
        singleClassImports.add(senderInterface);
        // TODO: application classes used in listener methods

        SortedSet<String> wildcardImports = new TreeSet<String>();
        for (Type singleClassImport : singleClassImports) {
            wildcardImports.add(singleClassImport.getPackage() + ".*");
        }
        return wildcardImports;
    }

    private String factoryInterfaceName() {
        return genericFactoryInterfaceName(listenerName());
    }

    private String genericFactoryInterfaceName(String t) {
        return factoryInterface.getSimpleName() + "<" + t + ">";
    }

    private String eventName() {
        return genericEventInterfaceName(listenerName());
    }

    private String senderName() {
        return genericSenderInterface(genericEventInterfaceName(listenerName()));
    }

    private String genericEventInterfaceName(String t) {
        return eventInterface.getSimpleName() + "<" + t + ">";
    }

    private String genericSenderInterface(String t) {
        return senderInterface.getSimpleName() + "<" + t + ">";
    }

    private String listenerName() {
        return listenerInterface.getSimpleName();
    }

    private String factoryName() {
        return listenerName() + "Factory";
    }

    private String frontendName() {
        return listenerName() + "ToEvent";
    }

    private String backendName() {
        return "EventTo" + listenerName();
    }

    private String eventWrapperName(Method method) {
        return StringUtils.capitalizeFirstLetter(method.getName()) + "Event";
    }

    private String fileForClass(String className) {
        return targetPackage.replace('.', '/') + "/" + className + ".java";
    }
}
