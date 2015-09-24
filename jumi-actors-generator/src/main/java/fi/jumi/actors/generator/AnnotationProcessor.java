// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import com.google.common.base.Throwables;
import fi.jumi.actors.generator.ast.*;
import fi.jumi.actors.generator.codegen.GeneratedClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.*;

import static javax.tools.Diagnostic.Kind.*;

@SupportedAnnotationTypes("fi.jumi.actors.generator.GenerateEventizer")
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateEventizer.class)) {
            if (element.getKind() == ElementKind.INTERFACE) {
                log().printMessage(NOTE, "Generating eventizers for " + element, element);
                try {
                    generateEventizers((TypeElement) element);
                } catch (Exception e) {
                    log().printMessage(ERROR, "Failed to generate eventizers for " + element +
                            "\n" + Throwables.getStackTraceAsString(e), element);
                }
            } else {
                log().printMessage(ERROR, "Only interfaces can be annotated with @" + GenerateEventizer.class.getSimpleName(), element);
            }
        }
        return false;
    }

    private void generateEventizers(TypeElement eventInterface) throws IOException {
        GenerateEventizer config = eventInterface.getAnnotation(GenerateEventizer.class);

        String targetPackage;
        if (!config.targetPackage().isEmpty()) {
            targetPackage = config.targetPackage();
        } else {
            PackageElement currentPackage = getPackage(eventInterface);
            targetPackage = currentPackage.getQualifiedName().toString();
        }

        if (config.useParentInterface()) {
            List<? extends TypeMirror> parentInterfaces = eventInterface.getInterfaces();
            if (parentInterfaces.size() != 1) {
                log().printMessage(ERROR, "Expected one parent interface, but had " + parentInterfaces.size(), eventInterface);
                return;
            }
            String parentInterface = parentInterfaces.get(0).toString();
            JavaFileObject sourceCode = findSourceCode(parentInterface);
            if (sourceCode == null) {
                log().printMessage(ERROR, "Could not find source code for " + parentInterface, eventInterface);
                return;
            }
            eventInterface = AstExtractor.getAst(parentInterface, sourceCode);
        }

        if (!hasValidActorMethods(eventInterface)) {
            return;
        }

        EventStubGenerator generator = new EventStubGenerator(eventInterface, targetPackage);
        generator.setGeneratorName(getClass().getName());

        for (GeneratedClass generated : generator.getGeneratedClasses()) {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(generated.name, eventInterface);
            Writer w = file.openWriter();
            w.write(generated.source);
            w.close();
        }
    }

    private static PackageElement getPackage(Element e) {
        while (e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return (PackageElement) e;
    }

    private static JavaFileObject findSourceCode(String className) {
        LibrarySourceLocator locator = new LibrarySourceLocator();
        for (; !className.isEmpty(); className = getEnclosingClass(className)) {
            String sourceCode = locator.findSources(className);
            if (sourceCode != null) {
                return new JavaSourceFromString(className, sourceCode);
            }
        }
        return null;
    }

    private static String getEnclosingClass(String className) {
        int index = className.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return className.substring(0, index);
    }

    private boolean hasValidActorMethods(TypeElement eventInterface) {
        boolean ok = true;
        for (Element element : eventInterface.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                TypeMirror returnType = method.getReturnType();
                if (returnType.getKind() != TypeKind.VOID) {
                    log().printMessage(ERROR, "Actor interface methods must return void, but method " + method + " returns " + returnType, method);
                    ok = false;
                }
                List<? extends TypeMirror> thrownTypes = method.getThrownTypes();
                if (!thrownTypes.isEmpty()) {
                    log().printMessage(ERROR, "Actor interface methods must not throw exceptions, but method " + method + " throws " + thrownTypes, method);
                    ok = false;
                }
            }
        }
        return ok;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // avoid warnings on newer JVMs, even though we compile on JDK 6
        return processingEnv.getSourceVersion();
    }

    private Messager log() {
        return processingEnv.getMessager();
    }
}
