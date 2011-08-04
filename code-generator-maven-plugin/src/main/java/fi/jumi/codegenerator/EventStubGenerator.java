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
    private Type eventInterfaceRaw;
    private Type factoryInterfaceRaw;
    private Type senderInterfaceRaw;

    // configuration

    public void setListenerType(Class<?> listenerType) {
        this.listenerType = listenerType;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public void setEventInterface(String eventInterface) {
        this.eventInterfaceRaw = new Type(eventInterface);
    }

    public void setFactoryInterface(String factoryInterface) {
        this.factoryInterfaceRaw = new Type(factoryInterface);
    }

    public void setSenderInterface(String senderInterface) {
        this.senderInterfaceRaw = new Type(senderInterface);
    }

    // public API

    public String getFactoryPath() {
        return fileForClass(myFactoryName());
    }

    public String getFactorySource() {
        StringBuilder sb = new StringBuilder();
        sb.append(packageStatement());
        sb.append(importStatements());

        // TODO: class(name, interface)?
        sb.append("public class " + myFactoryName() + " implements " + factoryName() + " {\n");
        sb.append("\n");

        sb.append("    public Class<" + listenerName() + "> getType() {\n");
        sb.append("        return " + listenerName() + ".class;\n");
        sb.append("    }\n");

        sb.append("\n");

        sb.append("    public " + listenerName() + " newFrontend(" + senderName() + " target) {\n");
        sb.append("        return new " + myFrontendName() + "(target);\n");
        sb.append("    }\n");

        sb.append("\n");

        // TODO: method(name, args, body)?
        // TODO: returnNewInstance(classname, args)?
        sb.append("    public " + senderName() + " newBackend(" + listenerName() + " target) {\n");
        sb.append("        return new " + myBackendName() + "(target);\n");
        sb.append("    }\n");

        sb.append("}\n");
        return sb.toString();
    }

    public String getFrontendPath() {
        return fileForClass(myFrontendName());
    }

    public String getFrontendSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(packageStatement());
        sb.append(importStatements());

        sb.append("public class " + myFrontendName() + " implements " + listenerName() + " {\n");
        sb.append("\n");

        sb.append(constructor(myFrontendName(), new Parameter(new Type(senderName()), "sender")));

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
        sb.append("        sender.send(new " + myEventWrapperName(method) + "(" + arguments.toActualArguments() + "));\n");
        sb.append("    }\n");
        return sb;
    }

    public String getBackendPath() {
        return fileForClass(myBackendName());
    }

    public String getBackendSource() {
        StringBuilder sb = new StringBuilder();
        sb.append(packageStatement());
        sb.append(importStatements());

        sb.append("public class " + myBackendName() + " implements " + senderName() + " {\n");
        sb.append("\n");

        Parameter listener = new Parameter(listenerInterface(), "listener");
        sb.append(constructor(myBackendName(), listener));

        sb.append("\n");

        sb.append("    public void send(" + eventName() + " message) {\n");
        sb.append("        message.fireOn(" + listener.name + ");\n");
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
        singleClassImports.add(listenerInterface());
        singleClassImports.add(eventInterfaceRaw);
        singleClassImports.add(factoryInterfaceRaw);
        singleClassImports.add(senderInterfaceRaw);
        // TODO: application classes used in listener methods

        SortedSet<String> wildcardImports = new TreeSet<String>();
        for (Type singleClassImport : singleClassImports) {
            wildcardImports.add(singleClassImport.getPackage() + ".*");
        }
        return wildcardImports;
    }

    private StringBuilder constructor(String className, Parameter arg0) {
        // TODO: generify to multiple arguments
        StringBuilder sb = new StringBuilder();
        sb.append("    private final " + arg0.type.getSimpleName() + " " + arg0.name + ";\n");
        sb.append("\n");

        sb.append("    public " + className + "(" + arg0.type.getSimpleName() + " " + arg0.name + ") {\n");
        sb.append("        this." + arg0.name + " = " + arg0.name + ";\n");
        sb.append("    }\n");
        return sb;
    }


    // names of generated classes

    private String myFactoryName() {
        return listenerName() + "Factory";
    }

    private String myFrontendName() {
        return listenerName() + "ToEvent";
    }

    private String myBackendName() {
        return "EventTo" + listenerName();
    }

    private String myEventWrapperName(Method method) {
        return StringUtils.capitalizeFirstLetter(method.getName()) + "Event";
    }

    private String fileForClass(String className) {
        return targetPackage.replace('.', '/') + "/" + className + ".java";
    }


    // names of parameter classes

    private String listenerName() {
        return listenerInterface().getSimpleName();
    }

    private Type listenerInterface() {
        return new Type(listenerType);
    }


    private String eventName() {
        return eventInterface().getSimpleName();
    }

    private Type eventInterface() {
        return eventInterface(listenerInterface());
    }

    private Type eventInterface(Type t) {
        return eventInterfaceRaw.withTypeParameter(t);
    }


    private String factoryName() {
        return factoryInterface().getSimpleName();
    }

    private Type factoryInterface() {
        return factoryInterface(listenerInterface());
    }

    private Type factoryInterface(Type t) {
        return factoryInterfaceRaw.withTypeParameter(t);
    }


    private String senderName() {
        return senderInterface().getSimpleName();
    }

    private Type senderInterface() {
        return senderInterface(eventInterface());
    }

    private Type senderInterface(Type t) {
        return senderInterfaceRaw.withTypeParameter(t);
    }
}
