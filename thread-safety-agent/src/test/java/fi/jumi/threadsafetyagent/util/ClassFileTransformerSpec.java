// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent.util;

import org.junit.Test;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClassFileTransformerSpec {

    @Test
    public void instruments_classes_with_the_provided_transformer() throws Exception {
        AbstractTransformationChain transformer = new AbstractTransformationChain() {
            protected ClassVisitor getAdapters(ClassVisitor cv) {
                return new AddEqualsMethodWhichReturnsTrue(cv);
            }
        };

        Object obj = createWithTransformer(ClassToInstrument.class, transformer);

        assertThat(obj.equals(new Object()), is(true));
    }

    @Test
    public void null_transformer_does_not_instrument_classes() throws Exception {
        NullClassFileTransformer transformer = new NullClassFileTransformer();

        Object obj = createWithTransformer(ClassToInstrument.class, transformer);

        assertThat(obj.equals(new Object()), is(false));
    }

    private static Object createWithTransformer(Class<?> clazz, ClassFileTransformer transformer) throws Exception {
        String className = clazz.getName();
        ClassLoader loader = new TransformationTestClassLoader(className, transformer);
        return loader.loadClass(className).newInstance();
    }


    private static class AddEqualsMethodWhichReturnsTrue extends ClassAdapter {

        public AddEqualsMethodWhichReturnsTrue(ClassVisitor cv) {
            super(cv);
        }

        public void visitEnd() {
            MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(1, 2);
            mv.visitEnd();
            super.visitEnd();
        }
    }

    public static class ClassToInstrument {
    }
}
