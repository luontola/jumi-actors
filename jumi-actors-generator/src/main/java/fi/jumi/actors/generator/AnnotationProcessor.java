// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import com.google.common.base.Throwables;
import fi.jumi.actors.generator.codegen.GeneratedClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.*;

@SupportedAnnotationTypes("fi.jumi.actors.generator.GenerateEventizer")
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager log = processingEnv.getMessager();
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateEventizer.class)) {
            if (element.getKind() == ElementKind.INTERFACE) {
                log.printMessage(NOTE, "Generating eventizers for " + element, element);
                try {
                    generateEventizers((TypeElement) element);
                } catch (Exception e) {
                    log.printMessage(ERROR, "Failed to generate eventizers for " + element +
                            "\n" + Throwables.getStackTraceAsString(e), element);
                }
            } else {
                log.printMessage(WARNING, "Only interfaces can be annotated with @GenerateEventizer", element);
            }
        }
        return false;
    }

    private void generateEventizers(TypeElement eventInterface) throws IOException {
        PackageElement pkg = getPackage(eventInterface);

        String targetPackage = pkg.getQualifiedName().toString();
        TargetPackageResolver targetPackageResolver = new TargetPackageResolver(targetPackage);
        EventStubGenerator generator = new EventStubGenerator(eventInterface, targetPackageResolver);

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

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // avoid warnings on newer JVMs, even though we compile on JDK 6
        return processingEnv.getSourceVersion();
    }
}
