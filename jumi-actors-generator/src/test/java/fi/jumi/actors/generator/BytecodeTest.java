// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.util.*;

import java.io.*;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("unchecked")
public class BytecodeTest {

    public static final int ORIGINAL_VALUE = 42;

    @Test
    public void show_it() throws Exception {
        ASMifier.main(new String[]{GuineaPig.class.getName()});
    }

    @Test
    public void do_it() throws Exception {
        Callable<Integer> obj = new GuineaPig();
        assertThat(obj.call(), is(ORIGINAL_VALUE));

        byte[] original = getBytecode(GuineaPig.class);
        byte[] transformed = transform(original);
        obj = (Callable<Integer>) new MyClassLoader().defineClass(transformed).newInstance();

        assertThat(obj.call(), is(ORIGINAL_VALUE));
    }

    private byte[] transform(byte[] original) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor next = writer;

        next = new MyClassVisitor(next);
        next = new TraceClassVisitor(next, new ASMifier(), new PrintWriter(System.out));

        new ClassReader(original).accept(next, 0);
        return writer.toByteArray();
    }

    private static class MyClassVisitor extends ClassVisitor {
        public MyClassVisitor(ClassVisitor next) {
            super(Opcodes.ASM4, next);
        }
    }


    // guinea pigs

    public static class GuineaPig implements Callable<Integer> {
        @Override
        public Integer call() {
            return ORIGINAL_VALUE;
        }
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
