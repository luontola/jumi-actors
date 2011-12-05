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

public class EventStubGenerator {

    private final String targetPackage;

    private final JavaType listenerInterface;
    private final Method[] listenerMethods;

    private final JavaType factoryInterface;
    private final JavaType eventInterface;
    private final JavaType senderInterface;

    public EventStubGenerator(Class<?> listenerType, String targetPackage) {
        this.targetPackage = targetPackage;

        listenerInterface = JavaType.of(listenerType);
        listenerMethods = listenerType.getMethods();
        Arrays.sort(listenerMethods, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });

        factoryInterface = JavaType.of(ListenerFactory.class, listenerInterface);
        eventInterface = JavaType.of(Event.class, listenerInterface);
        senderInterface = JavaType.of(MessageSender.class, eventInterface);
    }

    public GeneratedClass getFactory() {
        ClassBuilder cb = new ClassBuilder(myFactoryName(), targetPackage);
        cb.implement(factoryInterface);

        String listenerName = cb.getImportedName(listenerInterface);
        String senderName = cb.getImportedName(senderInterface);

        cb.addMethod("" +
                "    public Class<" + listenerName + "> getType() {\n" +
                "        return " + listenerName + ".class;\n" +
                "    }\n");

        cb.addMethod("" +
                "    public " + listenerName + " newFrontend(" + senderName + " target) {\n" +
                "        return new " + myFrontendName() + "(target);\n" +
                "    }\n");

        cb.addMethod("" +
                "    public " + senderName + " newBackend(" + listenerName + " target) {\n" +
                "        return new " + myBackendName() + "(target);\n" +
                "    }\n");

        return cb.build();
    }

    public GeneratedClass getFrontend() {
        Argument sender = new Argument(senderInterface, "sender");

        ClassBuilder cb = new ClassBuilder(myFrontendName(), targetPackage);
        cb.implement(listenerInterface);
        cb.fieldsAndConstructorParameters(new ArgumentList(sender));

        for (Method method : listenerMethods) {
            ArgumentList arguments = new ArgumentList(method);
            cb.addImport(arguments);
            cb.addMethod("" +
                    "    public void " + method.getName() + "(" + arguments.toFormalArguments() + ") {\n" +
                    "        " + sender.name + ".send(new " + myEventWrapperName(method) + "(" + arguments.toActualArguments() + "));\n" +
                    "    }\n");
        }
        return cb.build();
    }

    public GeneratedClass getBackend() {
        Argument listener = new Argument(listenerInterface, "listener");

        ClassBuilder cb = new ClassBuilder(myBackendName(), targetPackage);
        cb.implement(senderInterface);
        cb.fieldsAndConstructorParameters(new ArgumentList(listener));

        String eventName = cb.getImportedName(eventInterface);
        cb.addMethod("" +
                "    public void send(" + eventName + " message) {\n" +
                "        message.fireOn(" + listener.name + ");\n" +
                "    }\n");

        return cb.build();
    }

    public List<GeneratedClass> getEvents() {
        List<GeneratedClass> events = new ArrayList<GeneratedClass>();
        for (Method method : listenerMethods) {
            ArgumentList arguments = new ArgumentList(method);

            ClassBuilder cb = new ClassBuilder(myEventWrapperName(method), targetPackage);
            cb.implement(eventInterface);
            cb.implement(JavaType.of(Serializable.class));
            cb.fieldsAndConstructorParameters(arguments);

            String listenerName = cb.getImportedName(listenerInterface);
            cb.addImport(arguments);

            cb.addMethod("" +
                    "    public void fireOn(" + listenerName + " target) {\n" +
                    "        target." + method.getName() + "(" + arguments.toActualArguments() + ");\n" +
                    "    }\n");

            cb.addMethod("" +
                    "    public String toString() {\n" +
                    "        return \"" + listenerName + "." + method.getName() + "(" + arguments.toToString() + ")\";\n" +
                    "    }\n");

            events.add(cb.build());
        }

        Collections.sort(events, new Comparator<GeneratedClass>() {
            public int compare(GeneratedClass o1, GeneratedClass o2) {
                return o1.path.compareTo(o2.path);
            }
        });
        return events;
    }


    // names of generated classes

    private String myFactoryName() {
        return listenerInterface.getRawName() + "Factory";
    }

    private String myFrontendName() {
        return listenerInterface.getRawName() + "ToEvent";
    }

    private String myBackendName() {
        return "EventTo" + listenerInterface.getRawName();
    }

    private String myEventWrapperName(Method method) {
        return StringUtils.capitalizeFirstLetter(method.getName()) + "Event";
    }
}
