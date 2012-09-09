// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import fi.jumi.threadsafetyagent.util.DoNotTransformException;
import org.objectweb.asm.*;

import static java.lang.Math.max;
import static org.objectweb.asm.Opcodes.*;

public class AddThreadSafetyChecks extends ClassVisitor {

    private static final String CHECKER_CLASS = "fi/jumi/threadsafetyagent/ThreadSafetyChecker";
    private static final String CHECKER_CLASS_DESC = "L" + CHECKER_CLASS + ";";
    private static final String CHECKER_FIELD = "$Jumi$threadSafetyChecker";

    private String myClassName;
    private Label lastGeneratedCode;

    // TODO: keep an eye on what to do with stackmap frames when parsing & producing Java 7 bytecode
    // http://weblogs.java.net/blog/fabriziogiudici/archive/2012/05/07/understanding-subtle-new-behaviours-jdk-7
    // http://download.forge.objectweb.org/asm/asm4-guide.pdf
    //     3.1.5 pages 39-41: stack map frames explained
    //     3.2.1 page 44: ClassWriter options for computing them automatically (slower)

    public AddThreadSafetyChecks(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if ((access & ACC_INTERFACE) == ACC_INTERFACE) {
            throw new DoNotTransformException();
        }
        myClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (isConstructor(name)) {
            mv = new InstantiateChecker(api, mv);
        } else if (isInstanceMethod(access)) {
            mv = new CallChecker(api, mv);
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        createCheckerField();
        super.visitEnd();
    }


    private void createCheckerField() {
        FieldVisitor fv = this.visitField(ACC_PRIVATE + ACC_FINAL, CHECKER_FIELD, CHECKER_CLASS_DESC, null, null);
        fv.visitEnd();
    }

    private static boolean isConstructor(String name) {
        return name.equals("<init>");
    }

    private static boolean isInstanceMethod(int access) {
        return (access & ACC_STATIC) == 0;
    }


    // method transformers

    private class InstantiateChecker extends MethodVisitor {
        public InstantiateChecker(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                // insert to the end of the method
                super.visitVarInsn(ALOAD, 0);
                super.visitTypeInsn(NEW, CHECKER_CLASS);
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESPECIAL, CHECKER_CLASS, "<init>", "()V");
                super.visitFieldInsn(PUTFIELD, myClassName, CHECKER_FIELD, CHECKER_CLASS_DESC);
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // TODO: stack might not be empty right before a RETURN statement, so this maxStack can be too optimistic
            super.visitMaxs(max(3, maxStack), maxLocals);
        }
    }

    private class CallChecker extends MethodVisitor {
        public CallChecker(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // use line number of the first non-generated instruction
            lastGeneratedCode = new Label();
            super.visitLabel(lastGeneratedCode);

            // insert to the beginning of the method
            super.visitVarInsn(ALOAD, 0);
            super.visitFieldInsn(GETFIELD, myClassName, CHECKER_FIELD, CHECKER_CLASS_DESC);
            super.visitMethodInsn(INVOKEVIRTUAL, CHECKER_CLASS, "checkCurrentThread", "()V");
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            if (lastGeneratedCode != null) {
                super.visitLineNumber(line, lastGeneratedCode);
                lastGeneratedCode = null;
            }
            super.visitLineNumber(line, start);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(max(1, maxStack), maxLocals);
        }
    }
}
