// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.ast;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.*;
import java.util.*;

@SupportedAnnotationTypes("*")
public class AstExtractor extends AbstractProcessor {

    private static final String SKIP_COMPILE_PHASE = AstExtractor.class.getName() + ".SKIP_COMPILE_PHASE";

    private final String classNameToFind;
    private TypeElement result;

    public static TypeElement getAst(Class<?> clazz, JavaFileObject compilationUnit) {
        return getAst(clazz.getCanonicalName(), compilationUnit);
    }

    public static TypeElement getAst(String className, JavaFileObject compilationUnit) {
        AstExtractor astExtractor = new AstExtractor(className);
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(astExtractor, compilationUnit);
        if (!diagnostics.isEmpty()) {
            throw new RuntimeException("Failed to get AST of " + className + ": " + diagnostics);
        }
        return astExtractor.getResult();
    }

    private static List<Diagnostic<? extends JavaFileObject>> compile(AbstractProcessor processor, JavaFileObject... compilationUnits) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, Arrays.asList(compilationUnits));
        task.setProcessors(Collections.singletonList(processor));
        task.call();
        return getRealDiagnostics(diagnostics.getDiagnostics());
    }

    private static List<Diagnostic<? extends JavaFileObject>> getRealDiagnostics(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        List<Diagnostic<? extends JavaFileObject>> results = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            if (!diagnostic.getMessage(null).endsWith(SKIP_COMPILE_PHASE)) {
                results.add(diagnostic);
            }
        }
        return results;
    }

    private AstExtractor(String classNameToFind) {
        this.classNameToFind = classNameToFind;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion(); // avoid warnings about source version
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            result = searchIn(element);
        }
        // We don't need nor want the class to be compiled, we just want its AST,
        // so we'll prevent compiling it by producing a fake error message.
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, SKIP_COMPILE_PHASE);
        return false;
    }

    private TypeElement searchIn(Element haystack) {
        if (isMatch(classNameToFind, haystack)) {
            return (TypeElement) haystack;
        }
        for (Element enclosed : haystack.getEnclosedElements()) {
            TypeElement match = searchIn(enclosed);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private static boolean isMatch(String className, Element element) {
        return (element.getKind() == ElementKind.CLASS
                || element.getKind() == ElementKind.INTERFACE
                || element.getKind() == ElementKind.ANNOTATION_TYPE)
                && element.toString().equals(className);
    }

    public TypeElement getResult() {
        if (result == null) {
            throw new IllegalStateException("Did not find " + classNameToFind);
        }
        return result;
    }
}
