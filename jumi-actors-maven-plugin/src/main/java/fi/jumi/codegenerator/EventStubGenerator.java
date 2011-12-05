// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import fi.jumi.actors.*;
import fi.jumi.codegenerator.java.*;
import org.codehaus.plexus.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
public class EventStubGenerator {

    private final Class<?> listenerType;
    private final String targetPackage;
    private final Type eventInterfaceRaw = new Type(Event.class);
    private final Type factoryInterfaceRaw = new Type(ListenerFactory.class);
    private final Type senderInterfaceRaw = new Type(MessageSender.class);

    public EventStubGenerator(Class<?> listenerType, String targetPackage) {
        this.listenerType = listenerType;
        this.targetPackage = targetPackage;
    }

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
        source.append(classBody(className, new ArgumentList(), methods, factoryInterface()));

        return new GeneratedClass(fileForClass(className), source.toString());
    }

    public GeneratedClass getFrontend() {
        String className = myFrontendName();
        Argument sender = new Argument(senderInterface(), "sender");

        // TODO: extract a domain class to represent methods?
        StringBuilder methods = new StringBuilder();
        boolean first = true;
        for (Method method : listenerMethods()) {
            if (!first) {
                methods.append("\n");
            }
            first = false;
            methods.append(delegateMethodToSender(method, sender));
        }

        StringBuilder source = new StringBuilder();
        source.append(packageStatement());
        source.append(importStatements());
        source.append(classBody(className, new ArgumentList(sender), methods, listenerInterface()));

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
        source.append(classBody(className, new ArgumentList(listener), methods, senderInterface()));

        return new GeneratedClass(fileForClass(className), source.toString());
    }

    public List<GeneratedClass> getEvents() {
        List<GeneratedClass> events = new ArrayList<GeneratedClass>();
        for (Method method : listenerMethods()) {
            String className = myEventWrapperName(method);
            ArgumentList arguments = new ArgumentList(method);

            StringBuilder methods = new StringBuilder();
            methods.append("    public void fireOn(" + listenerName() + " target) {\n");
            methods.append("        target." + method.getName() + "(" + arguments.toActualArguments() + ");\n");
            methods.append("    }\n");

            methods.append("\n");
            methods.append("    public String toString() {\n");
            methods.append("        return \"" + listenerName() + "." + method.getName() + "(" + arguments.toToString() + ")\";\n");
            methods.append("    }\n");

            Type serializableInterface = new Type(Serializable.class);

            StringBuilder source = new StringBuilder();
            source.append(packageStatement());
            source.append(importStatements(serializableInterface));
            source.append(classBody(className, arguments, methods, eventInterface(), serializableInterface));

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

    private StringBuilder importStatements(Type... moreImports) {
        // TODO: extract class Imports
        // TODO: do not add unnecessary imports, but check that which ones are really needed by the current class
        List<Type> imports = new ArrayList<Type>();
        for (Method method : listenerMethods()) {
            for (Class<?> clazz : method.getParameterTypes()) {
                imports.add(new Type(clazz));
            }
        }
        Collections.addAll(imports, moreImports);

        StringBuilder sb = new StringBuilder();
        for (String classToImport : classesToImport(imports)) {
            sb.append("import " + classToImport + ";\n");
        }
        sb.append("\n");
        return sb;
    }

    private Collection<String> classesToImport(List<Type> additionalImports) {
        SortedSet<Type> singleClassImports = new TreeSet<Type>();
        singleClassImports.add(listenerInterface());
        singleClassImports.add(eventInterfaceRaw);
        singleClassImports.add(factoryInterfaceRaw);
        singleClassImports.add(senderInterfaceRaw);
        singleClassImports.addAll(additionalImports);

        SortedSet<String> wildcardImports = new TreeSet<String>();
        for (Type singleClassImport : singleClassImports) {
            wildcardImports.add(singleClassImport.getPackage() + ".*");
        }
        wildcardImports.remove("java.lang.*");
        return wildcardImports;
    }

    private StringBuilder classBody(String className, ArgumentList fields, StringBuilder methods, Type... interfaces) {
        StringBuilder sb = new StringBuilder();
        sb.append("public class " + className + " implements " + toImplementsDeclaration(interfaces) + " {\n");
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

    private static StringBuilder toImplementsDeclaration(Type[] types) {
        StringBuilder sb = new StringBuilder();
        for (Type type : types) {
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


    // other helper methods

    private Method[] listenerMethods() {
        Method[] methods = listenerType.getMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        return methods;
    }
}
