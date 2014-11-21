// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Set;

@SupportedAnnotationTypes("*")
public class AstExtractor extends AbstractProcessor {

    private final Class<?> search;
    private TypeElement result;

    public AstExtractor(Class<?> search) {
        this.search = search;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion(); // avoid warnings about source version
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            result = find(search, element);
        }
        return false;
    }

    private static TypeElement find(Class<?> needle, Element haystack) {
        if (isMatch(needle, haystack)) {
            return (TypeElement) haystack;
        }
        for (Element enclosed : haystack.getEnclosedElements()) {
            TypeElement match = find(needle, enclosed);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private static boolean isMatch(Class<?> type, Element element) {
        return (element.getKind() == ElementKind.CLASS
                || element.getKind() == ElementKind.INTERFACE
                || element.getKind() == ElementKind.ANNOTATION_TYPE)
                && element.toString().equals(type.getCanonicalName());
    }

    public TypeElement getResult() {
        if (result == null) {
            throw new IllegalStateException("Did not find " + search);
        }
        return result;
    }
}
