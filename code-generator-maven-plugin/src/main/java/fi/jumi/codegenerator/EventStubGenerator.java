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

    public GeneratedClass getFactory() {
        String className = myFactoryName();
        StringBuilder methods = new StringBuilder();

        methods.append("    public Class<" + listenerName() + "> getType() {\n");
        methods.append("        return " + listenerName() + ".class;\n");
        methods.append("    }\n");

        methods.append("\n");

        methods.append("    public " + listenerName() + " newFrontend(" + senderName() + " target) {\n");
        methods.append("        return new " + myFrontendName() + "(target);\n");
        methods.append("    }\n");

        methods.append("\n");

        // TODO: method(name, args, body)?
        // TODO: returnNewInstance(classname, args)?
        methods.append("    public " + senderName() + " newBackend(" + listenerName() + " target) {\n");
        methods.append("        return new " + myBackendName() + "(target);\n");
        methods.append("    }\n");

        StringBuilder source = new StringBuilder();
        source.append(packageStatement());
        source.append(importStatements());
        source.append(classBody(className, factoryInterface(), new ArgumentList(), methods));

        return new GeneratedClass(fileForClass(className), source.toString());
    }

    public GeneratedClass getFrontend() {
        String className = myFrontendName();
        Argument sender = new Argument(senderInterface(), "sender");

        // TODO: extract a domain class to represent methods?
        StringBuilder methods = new StringBuilder();
        for (Method method : listenerType.getMethods()) {
            methods.append(delegateMethodToSender(method, sender));
        }

        StringBuilder source = new StringBuilder();
        source.append(packageStatement());
        source.append(importStatements());
        source.append(classBody(className, listenerInterface(), new ArgumentList(sender), methods));

        return new GeneratedClass(fileForClass(className), source.toString());
    }

    private StringBuilder delegateMethodToSender(Method method, Argument sender) {
        ArgumentList arguments = new ArgumentList(method);
        StringBuilder sb = new StringBuilder();
        sb.append("    public void " + method.getName() + "(" + arguments.toFormalArguments() + ") {\n");
        sb.append("        " + sender.name + ".send(new " + myEventWrapperName(method) + "(" + arguments.toActualArguments() + "));\n");
        sb.append("    }\n");
        return sb;
    }

    public GeneratedClass getBackend() {
        String className = myBackendName();
        Argument listener = new Argument(listenerInterface(), "listener");

        StringBuilder methods = new StringBuilder();
        methods.append("    public void send(" + eventName() + " message) {\n");
        methods.append("        message.fireOn(" + listener.name + ");\n");
        methods.append("    }\n");

        StringBuilder source = new StringBuilder();
        source.append(packageStatement());
        source.append(importStatements());
        source.append(classBody(className, senderInterface(), new ArgumentList(listener), methods));

        return new GeneratedClass(fileForClass(className), source.toString());
    }

    public List<GeneratedClass> getEvents() {
        List<GeneratedClass> events = new ArrayList<GeneratedClass>();
        for (Method method : listenerType.getMethods()) {
            String className = myEventWrapperName(method);
            ArgumentList arguments = new ArgumentList(method);

            StringBuilder methods = new StringBuilder();
            methods.append("    public void fireOn(" + listenerName() + " target) {\n");
            methods.append("        target." + method.getName() + "(" + arguments.toActualArguments() + ");\n");
            methods.append("    }\n");

            StringBuilder source = new StringBuilder();
            source.append(packageStatement());
            source.append(importStatements());
            source.append(classBody(className, eventInterface(), arguments, methods));

            events.add(new GeneratedClass(fileForClass(className), source.toString()));
        }

        Collections.sort(events, new Comparator<GeneratedClass>() {
            public int compare(GeneratedClass o1, GeneratedClass o2) {
                return o1.path.compareTo(o2.path);
            }
        });
        return events;
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

    private StringBuilder classBody(String className, Type myInterface, ArgumentList fields, StringBuilder methods) {
        StringBuilder sb = new StringBuilder();
        sb.append("public class " + className + " implements " + myInterface.getSimpleName() + " {\n");
        sb.append("\n");
        if (fields.size() > 0) {
            sb.append(fields(fields));
            sb.append("\n");
            sb.append(constructor(className, fields));
            sb.append("\n");
        }
        sb.append(methods);
        sb.append("}\n");
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
