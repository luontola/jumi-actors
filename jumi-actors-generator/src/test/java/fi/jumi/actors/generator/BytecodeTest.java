// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.util.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.objectweb.asm.Opcodes.*;

public class BytecodeTest {

    @Test
    public void transform_byte() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 10;
            }
        }).call(), is(20));
    }

    @Test
    public void transform_byte_to_short() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 100;
            }
        }).call(), is(200));
    }

    @Test
    public void transform_short() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1000;
            }
        }).call(), is(2000));
    }

    @Test
    public void transform_short_to_int() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 30000;
            }
        }).call(), is(60000));
    }

    @Test
    public void transform_int() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1000000;
            }
        }).call(), is(2000000));
    }

    @Test
    public void transform_zero() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 0;
            }
        }).call(), is(0));
    }

    @Test
    public void transform_one() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }).call(), is(2));
    }

    @Test
    public void transform_two() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 2;
            }
        }).call(), is(4));
    }

    @Test
    public void transform_three() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 3;
            }
        }).call(), is(6));
    }

    @Test
    public void transform_four() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 4;
            }
        }).call(), is(8));
    }

    @Test
    public void transform_five() throws Exception {
        assertThat(transform(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 5;
            }
        }).call(), is(10));
    }

    @Test
    public void transform_boolean() throws Exception {
        assertThat(transform(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        }).call(), is(true));
    }

    @SuppressWarnings("unchecked")
    private <T> Callable<T> transform(Callable<T> obj) throws Exception {
        byte[] original = getBytecode(obj.getClass());
        byte[] transformed = transform(original);
        Class<?> aClass = new MyClassLoader().defineClass(transformed);
        Constructor<?> constructor = aClass.getDeclaredConstructor(getClass());
        constructor.setAccessible(true);
        obj = (Callable<T>) constructor.newInstance(this);
        return obj;
    }

    private static byte[] transform(byte[] original) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor next = writer;

        next = new MultiplyConstantsByTwo(next);
        next = new TraceClassVisitor(next, new ASMifier(), new PrintWriter(System.out));

        new ClassReader(original).accept(next, 0);
        return writer.toByteArray();
    }


    // helpers

    private static byte[] getBytecode(Class<?> clazz) throws IOException {
        String path = clazz.getName().replace('.', '/') + ".class";
        InputStream in = clazz.getClassLoader().getResourceAsStream(path);
        try {
            return ByteStreams.toByteArray(in);
        } finally {
            in.close();
        }
    }

    private static class MyClassLoader extends ClassLoader {
        public Class<?> defineClass(byte[] bytecode) {
            return defineClass(null, bytecode, 0, bytecode.length);
        }
    }
}

class MultiplyConstantsByTwo extends ClassVisitor {

    public MultiplyConstantsByTwo(ClassVisitor next) {
        super(ASM4, next);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor next = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(ASM4, next) {
            @Override
            public void visitIntInsn(int opcode, int operand) {
                if (opcode == BIPUSH || opcode == SIPUSH) {
                    operand *= 2;
                    if (operand > Byte.MAX_VALUE) {
                        opcode = SIPUSH;
                    }
                    if (operand > Short.MAX_VALUE) {
                        super.visitLdcInsn(operand);
                        return;
                    }
                }
                super.visitIntInsn(opcode, operand);
            }

            @Override
            public void visitLdcInsn(Object cst) {
                if (cst instanceof Integer) {
                    cst = ((Integer) cst) * 2;
                }
                super.visitLdcInsn(cst);
            }

            @Override
            public void visitInsn(int opcode) {
                switch (opcode) {
                    case ICONST_1:
                        super.visitInsn(ICONST_2);
                        break;
                    case ICONST_2:
                        super.visitInsn(ICONST_4);
                        break;
                    case ICONST_3:
                        super.visitIntInsn(BIPUSH, 6);
                        break;
                    case ICONST_4:
                        super.visitIntInsn(BIPUSH, 8);
                        break;
                    case ICONST_5:
                        super.visitIntInsn(BIPUSH, 10);
                        break;
                    default:
                        super.visitInsn(opcode);
                        break;
                }
            }
        };
    }
}
