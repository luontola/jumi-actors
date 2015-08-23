// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.ast;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Set;

@SupportedAnnotationTypes("*")
public class AstExtractor extends AbstractProcessor {

    private final String classNameToFind;
    private TypeElement result;

    public AstExtractor(Class<?> classToFind) {
        this(classToFind.getCanonicalName());
    }

    public AstExtractor(String classNameToFind) {
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
