// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import fi.jumi.actors.eventizers.*;
import fi.jumi.actors.generator.codegen.*;
import fi.jumi.actors.queue.MessageSender;

import javax.annotation.Generated;
import javax.lang.model.element.TypeElement;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.CaseFormat.*;

public class EventStubGenerator {

    private final JavaType listenerInterface;
    private final List<JavaMethod> listenerMethods;

    private final JavaType eventizerInterface;
    private final JavaType eventInterface;
    private final JavaType senderInterface;

    private final String eventizerPackage;
    private final String stubsPackage;
    private String generatorName = getClass().getName();
    private Date generationDate = new Date();

    public EventStubGenerator(TypeElement listenerType, String targetPackage) {
        listenerInterface = JavaType.of(listenerType);
        listenerMethods = JavaClasses.getMethods(listenerType);

        eventizerInterface = JavaType.of(Eventizer.class, listenerInterface);
        eventInterface = JavaType.of(Event.class, listenerInterface);
        senderInterface = JavaType.of(MessageSender.class, eventInterface);

        eventizerPackage = targetPackage;
        stubsPackage = targetPackage + "." + UPPER_CAMEL.to(LOWER_CAMEL, listenerInterface.getRawSimpleName());
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public List<GeneratedClass> getGeneratedClasses() {
        List<GeneratedClass> generated = new ArrayList<>();
        generated.add(getEventizer());
        generated.add(getFrontend());
        generated.add(getBackend());
        generated.addAll(getEvents());
        return generated;
    }

    public GeneratedClass getEventizer() {
        ClassBuilder cb = new ClassBuilder(myEventizerName(), eventizerPackage);
        addGeneratedAnnotation(cb);
        cb.implement(eventizerInterface);
        cb.imports.addPackageImport(stubsPackage);

        String listenerType = cb.imports.getSimpleName(listenerInterface);
        String senderType = cb.imports.getSimpleName(senderInterface);

        cb.addMethod("" +
                "    @Override\n" +
                "    public Class<" + listenerType + "> getType() {\n" +
                "        return " + listenerType + ".class;\n" +
                "    }\n");

        cb.addMethod("" +
                "    @Override\n" +
                "    public " + listenerType + " newFrontend(" + senderType + " target) {\n" +
                "        return new " + myFrontendName() + "(target);\n" +
                "    }\n");

        cb.addMethod("" +
                "    @Override\n" +
                "    public " + senderType + " newBackend(" + listenerType + " target) {\n" +
                "        return new " + myBackendName() + "(target);\n" +
                "    }\n");

        return cb.build();
    }

    public GeneratedClass getFrontend() {
        JavaVar target = JavaVar.of(senderInterface, "target");

        ClassBuilder cb = new ClassBuilder(myFrontendName(), stubsPackage);
        addGeneratedAnnotation(cb);
        cb.implement(listenerInterface);
        cb.fieldsAndConstructorParameters(Arrays.asList(target));

        for (JavaMethod method : listenerMethods) {
            cb.addMethod("" +
                    "    @Override\n" +
                    "    public void " + method.getName() + "(" + JavaVar.toFormalArguments(method.getArguments(), cb.imports) + ") {\n" +
                    "        " + target.getName() + ".send(new " + myEventWrapperName(method) + "(" + JavaVar.toActualArguments(method.getArguments()) + "));\n" +
                    "    }\n");
        }
        return cb.build();
    }

    public GeneratedClass getBackend() {
        JavaVar target = JavaVar.of(listenerInterface, "target");

        ClassBuilder cb = new ClassBuilder(myBackendName(), stubsPackage);
        addGeneratedAnnotation(cb);
        cb.implement(senderInterface);
        cb.fieldsAndConstructorParameters(Arrays.asList(target));

        cb.addMethod("" +
                "    @Override\n" +
                "    public void send(" + cb.imports.getSimpleName(eventInterface) + " message) {\n" +
                "        message.fireOn(" + target.getName() + ");\n" +
                "    }\n");

        return cb.build();
    }

    public List<GeneratedClass> getEvents() {
        List<GeneratedClass> events = new ArrayList<>();
        for (JavaMethod method : listenerMethods) {
            List<JavaVar> arguments = method.getArguments();

            ClassBuilder cb = new ClassBuilder(myEventWrapperName(method), stubsPackage);
            addGeneratedAnnotation(cb);
            cb.implement(eventInterface);
            cb.implement(JavaType.of(Serializable.class));
            cb.fieldsAndConstructorParameters(arguments);

            String eventToString = cb.imports.getSimpleName(JavaType.of(EventToString.class));
            String listenerName = cb.imports.getSimpleName(listenerInterface);

            for (JavaVar argument : arguments) {
                cb.addMethod("" +
                        "    public " + cb.imports.getSimpleName(argument.getType()) + " " + getterName(argument.getName()) + "() {\n" +
                        "        return " + argument.getName() + ";\n" +
                        "    }\n");
            }

            cb.addMethod("" +
                    "    @Override\n" +
                    "    public void fireOn(" + listenerName + " target) {\n" +
                    "        target." + method.getName() + "(" + JavaVar.toActualArguments(arguments) + ");\n" +
                    "    }\n");

            cb.addMethod("" +
                    "    @Override\n" +
                    "    public String toString() {\n" +
                    "        return " + eventToString + ".format(\"" + listenerName + "\", \"" + method.getName() + "\"" + JavaVar.toActualVarargs(arguments) + ");\n" +
                    "    }\n");

            events.add(cb.build());
        }

        Collections.sort(events, (o1, o2) -> o1.name.compareTo(o2.name));
        return events;
    }

    private void addGeneratedAnnotation(ClassBuilder cb) {
        cb.annotate("@" + cb.imports.getSimpleName(JavaType.of(Generated.class)) +
                "(value = \"" + generatorName + "\",\n" +
                "        comments = \"Based on " + listenerInterface.getCanonicalName() + "\",\n" +
                "        date = \"" + new SimpleDateFormat("yyyy-MM-dd").format(generationDate) + "\")");
    }


    // names of generated classes

    private String myEventizerName() {
        return listenerInterface.getRawSimpleName() + "Eventizer";
    }

    private String myFrontendName() {
        return listenerInterface.getRawSimpleName() + "ToEvent";
    }

    private String myBackendName() {
        return "EventTo" + listenerInterface.getRawSimpleName();
    }

    private String myEventWrapperName(JavaMethod method) {
        return LOWER_CAMEL.to(UPPER_CAMEL, method.getName()) + "Event";
    }

    private static String getterName(String field) {
        return "get" + LOWER_CAMEL.to(UPPER_CAMEL, field);
    }
}
