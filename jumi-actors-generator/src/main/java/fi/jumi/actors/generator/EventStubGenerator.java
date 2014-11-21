// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import fi.jumi.actors.eventizers.*;
import fi.jumi.actors.generator.codegen.*;
import fi.jumi.actors.queue.MessageSender;

import javax.lang.model.element.TypeElement;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.base.CaseFormat.*;

public class EventStubGenerator {

    private final JavaType listenerInterface;
    private final Method[] listenerMethods;

    private final JavaType eventizerInterface;
    private final JavaType eventInterface;
    private final JavaType senderInterface;

    private final String eventizerPackage;
    private final String stubsPackage;

    public EventStubGenerator(Class<?> listenerType1, TypeElement listenerType, TargetPackageResolver targetPackageResolver) {
        Eventizers.validateActorInterface(listenerType1);
        listenerInterface = JavaType.of(listenerType1);
        listenerMethods = listenerType1.getMethods();
        Arrays.sort(listenerMethods, new Comparator<Method>() {
            @Override
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });

        eventizerInterface = JavaType.of(Eventizer.class, listenerInterface);
        eventInterface = JavaType.of(Event.class, listenerInterface);
        senderInterface = JavaType.of(MessageSender.class, eventInterface);

        eventizerPackage = targetPackageResolver.getEventizerPackage();
        stubsPackage = targetPackageResolver.getStubsPackage(listenerInterface);
    }

    public GeneratedClass getEventizer() {
        ClassBuilder cb = new ClassBuilder(myEventizerName(), eventizerPackage);
        cb.implement(eventizerInterface);
        cb.addPackageImport(stubsPackage);

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

        ClassBuilder cb = new ClassBuilder(myFrontendName(), stubsPackage);
        cb.implement(listenerInterface);
        cb.fieldsAndConstructorParameters(new ArgumentList(sender));

        for (Method method : listenerMethods) {
            ArgumentList arguments = new ArgumentList(method);
            cb.addImports(arguments);
            cb.addMethod("" +
                    "    public void " + method.getName() + "(" + arguments.toFormalArguments() + ") {\n" +
                    "        " + sender.name + ".send(new " + myEventWrapperName(method) + "(" + arguments.toActualArguments() + "));\n" +
                    "    }\n");
        }
        return cb.build();
    }

    public GeneratedClass getBackend() {
        Argument listener = new Argument(listenerInterface, "listener");

        ClassBuilder cb = new ClassBuilder(myBackendName(), stubsPackage);
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

            ClassBuilder cb = new ClassBuilder(myEventWrapperName(method), stubsPackage);
            cb.implement(eventInterface);
            cb.implement(JavaType.of(Serializable.class));
            cb.fieldsAndConstructorParameters(arguments);

            String eventToString = cb.getImportedName(JavaType.of(EventToString.class));
            String listenerName = cb.getImportedName(listenerInterface);
            cb.addImports(arguments);

            cb.addMethod("" +
                    "    public void fireOn(" + listenerName + " target) {\n" +
                    "        target." + method.getName() + "(" + arguments.toActualArguments() + ");\n" +
                    "    }\n");

            cb.addMethod("" +
                    "    public String toString() {\n" +
                    "        return " + eventToString + ".format(\"" + listenerName + "\", \"" + method.getName() + "\"" + arguments.toActualVarargs() + ");\n" +
                    "    }\n");

            events.add(cb.build());
        }

        Collections.sort(events, new Comparator<GeneratedClass>() {
            @Override
            public int compare(GeneratedClass o1, GeneratedClass o2) {
                return o1.path.compareTo(o2.path);
            }
        });
        return events;
    }


    // names of generated classes

    private String myEventizerName() {
        return listenerInterface.getRawName() + "Eventizer";
    }

    private String myFrontendName() {
        return listenerInterface.getRawName() + "ToEvent";
    }

    private String myBackendName() {
        return "EventTo" + listenerInterface.getRawName();
    }

    private String myEventWrapperName(Method method) {
        return LOWER_CAMEL.to(UPPER_CAMEL, method.getName()) + "Event";
    }
}
