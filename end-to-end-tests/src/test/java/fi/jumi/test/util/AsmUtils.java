// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.hamcrest.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;

public class AsmUtils {

    public static ClassNode readClass(InputStream in) throws IOException {
        ClassNode cn = new ClassNode();
        ClassReader cr = new ClassReader(in);
        cr.accept(cn, 0);
        return cn;
    }

    // checking annotations

    public static Matcher<ClassNode> annotatedWithOneOf(final Class<?>... expectedAnnotations) {
        final List<String> expected = new ArrayList<String>();
        for (Class<?> annotation : expectedAnnotations) {
            expected.add(annotation.getName());
        }
        return new TypeSafeMatcher<ClassNode>() {
            protected boolean matchesSafely(ClassNode item) {
                List<String> actual = getAnnotations(item);
                return !Collections.disjoint(actual, expected);
            }

            public void describeTo(Description description) {
                description.appendText("annotated with one of ").appendText(expected.toString());
            }

            @Override
            protected void describeMismatchSafely(ClassNode item, Description mismatchDescription) {
                List<String> actual = getAnnotations(item);
                mismatchDescription
                        .appendText("class <").appendText(getClassName(item))
                        .appendText("> was annotated with ").appendText(actual.toString());
            }
        };
    }

    private static List<String> getAnnotations(ClassNode cn) {
        List<String> classNames = new ArrayList<String>();
        for (AnnotationNode annotation : asAnnotationNodeList(cn.visibleAnnotations)) {
            classNames.add(getClassName(annotation));
        }
        for (AnnotationNode annotation : asAnnotationNodeList(cn.invisibleAnnotations)) {
            classNames.add(getClassName(annotation));
        }
        return classNames;
    }

    @SuppressWarnings("unchecked")
    private static List<AnnotationNode> asAnnotationNodeList(List<?> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return (List<AnnotationNode>) list;
    }

    private static String getClassName(AnnotationNode annotation) {
        return Type.getType(annotation.desc).getClassName();
    }

    private static String getClassName(ClassNode cn) {
        return Type.getObjectType(cn.name).getClassName();
    }

    // access modifiers

    public static boolean isInterface(ClassNode cn) {
        return hasFlag(cn.access, Opcodes.ACC_INTERFACE);
    }

    public static boolean isSynthetic(ClassNode cn) {
        return hasFlag(cn.access, Opcodes.ACC_SYNTHETIC);
    }

    private static boolean hasFlag(int value, int flag) {
        return (value & flag) == flag;
    }
}
