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
import javax.lang.model.type.TypeMirror;
import javax.tools.*;
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
                log().printMessage(WARNING, "Only interfaces can be annotated with @GenerateEventizer", element);
            }
        }
        return false;
    }

    private void generateEventizers(TypeElement eventInterface) throws IOException {
        GenerateEventizer config = eventInterface.getAnnotation(GenerateEventizer.class);

        PackageElement pkg = getPackage(eventInterface);
        String targetPackage = pkg.getQualifiedName().toString();

        if (config.useParentInterface()) {
            List<? extends TypeMirror> parentInterfaces = eventInterface.getInterfaces();
            if (parentInterfaces.size() != 1) {
                log().printMessage(ERROR, "Expected one parent interface, but had " + parentInterfaces.size(), eventInterface);
                return;
            }
            String parentInterface = parentInterfaces.get(0).toString();
            String sourceCode = new LibrarySourceLocator().findSources(parentInterface);
            if (sourceCode == null) {
                log().printMessage(ERROR, "Could not find source code for " + parentInterface, eventInterface);
                return;
            }
            eventInterface = getAst(parentInterface, sourceCode);
        }

        TargetPackageResolver targetPackageResolver = new TargetPackageResolver(targetPackage);
        EventStubGenerator generator = new EventStubGenerator(eventInterface, targetPackageResolver);
        generator.setGeneratorName(getClass().getName());

        for (GeneratedClass generated : generator.getGeneratedClasses()) {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(generated.name, eventInterface);
            Writer w = file.openWriter();
            w.write(generated.source);
            w.close();
        }
    }

    private TypeElement getAst(String className, String sourceCode) {
        AstExtractor astExtractor = new AstExtractor(className);
        compile(astExtractor, new JavaSourceFromString(className, sourceCode));
        return astExtractor.getResult();
    }

    private void compile(AbstractProcessor processor, JavaFileObject... compilationUnits) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, Arrays.asList(compilationUnits));
        task.setProcessors(Collections.singletonList(processor));
        boolean success = task.call();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            log().printMessage(diagnostic.getKind(), diagnostic.getMessage(null));
        }
        if (!success) {
            throw new RuntimeException("Failed to compile " + Arrays.toString(compilationUnits));
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

    private Messager log() {
        return processingEnv.getMessager();
    }
}
