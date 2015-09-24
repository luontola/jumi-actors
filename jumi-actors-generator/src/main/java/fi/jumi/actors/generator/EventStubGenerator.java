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

    public EventStubGenerator(TypeElement listenerType, TargetPackageResolver targetPackageResolver) {
        listenerInterface = JavaType.of(listenerType);
        listenerMethods = JavaClasses.getMethods(listenerType);

        eventizerInterface = JavaType.of(Eventizer.class, listenerInterface);
        eventInterface = JavaType.of(Event.class, listenerInterface);
        senderInterface = JavaType.of(MessageSender.class, eventInterface);

        eventizerPackage = targetPackageResolver.getEventizerPackage();
        stubsPackage = targetPackageResolver.getStubsPackage(listenerInterface);
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public List<GeneratedClass> getGeneratedClasses() {
        List<GeneratedClass> generated = new ArrayList<GeneratedClass>();
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
        cb.addPackageImport(stubsPackage);

        String listenerType = cb.imported(listenerInterface);
        String senderType = cb.imported(senderInterface);

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
                    "    public void " + method.getName() + "(" + JavaVar.toFormalArguments(cb, method.getArguments()) + ") {\n" +
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
                "    public void send(" + cb.imported(eventInterface) + " message) {\n" +
                "        message.fireOn(" + target.getName() + ");\n" +
                "    }\n");

        return cb.build();
    }

    public List<GeneratedClass> getEvents() {
        List<GeneratedClass> events = new ArrayList<GeneratedClass>();
        for (JavaMethod method : listenerMethods) {
            List<JavaVar> arguments = method.getArguments();

            ClassBuilder cb = new ClassBuilder(myEventWrapperName(method), stubsPackage);
            addGeneratedAnnotation(cb);
            cb.implement(eventInterface);
            cb.implement(JavaType.of(Serializable.class));
            cb.fieldsAndConstructorParameters(arguments);

            String eventToString = cb.imported(JavaType.of(EventToString.class));
            String listenerName = cb.imported(listenerInterface);

            for (JavaVar argument : arguments) {
                cb.addMethod("" +
                        "    public " + cb.imported(argument.getType()) + " " + getterName(argument.getName()) + "() {\n" +
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

        Collections.sort(events, new Comparator<GeneratedClass>() {
            @Override
            public int compare(GeneratedClass o1, GeneratedClass o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        return events;
    }

    private void addGeneratedAnnotation(ClassBuilder cb) {
        cb.annotate("@" + cb.imported(JavaType.of(Generated.class)) +
                "(value = \"" + generatorName + "\",\n" +
                "        comments = \"Based on " + listenerInterface.getName() + "\",\n" +
                "        date = \"" + new SimpleDateFormat("yyyy-MM-dd").format(generationDate) + "\")");
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

    private String myEventWrapperName(JavaMethod method) {
        return LOWER_CAMEL.to(UPPER_CAMEL, method.getName()) + "Event";
    }

    private static String getterName(String field) {
        return "get" + LOWER_CAMEL.to(UPPER_CAMEL, field);
    }
}
