// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import org.objectweb.asm.*;

import static java.lang.Math.max;
import static org.objectweb.asm.Opcodes.*;

public class AddThreadSafetyChecks extends ClassAdapter {

    private static final String CHECKER_CLASS = "fi/jumi/threadsafetyagent/ThreadSafetyChecker";
    private static final String CHECKER_CLASS_DESC = "L" + CHECKER_CLASS + ";";
    private static final String CHECKER_FIELD = "$Jumi$threadSafetyChecker";

    private String myClassName;

    public AddThreadSafetyChecks(ClassVisitor cv) {
        super(cv);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        myClassName = name;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<init>")) {
            mv = new InstantiateChecker(mv);
        } else {
            // TODO: ignore static fields
            mv = new CallChecker(mv);
        }
        return mv;
    }

    public void visitEnd() {
        createCheckerField();
        super.visitEnd();
    }

    private void createCheckerField() {
        FieldVisitor fv = this.visitField(ACC_PRIVATE + ACC_FINAL, CHECKER_FIELD, CHECKER_CLASS_DESC, null, null);
        fv.visitEnd();
    }


    private class InstantiateChecker extends MethodAdapter {
        public InstantiateChecker(MethodVisitor mv) {
            super(mv);
        }

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

        public void visitMaxs(int maxStack, int maxLocals) {
            // TODO: stack might not be empty right before a RETURN statement, so this maxStack can be too optimistic
            super.visitMaxs(max(3, maxStack), maxLocals);
        }
    }

    private class CallChecker extends MethodAdapter {
        public CallChecker(MethodVisitor mv) {
            super(mv);
        }

        public void visitCode() {
            super.visitCode();

            // insert to the beginning of the method
            super.visitVarInsn(ALOAD, 0);
            super.visitFieldInsn(GETFIELD, myClassName, CHECKER_FIELD, CHECKER_CLASS_DESC);
            super.visitMethodInsn(INVOKEVIRTUAL, CHECKER_CLASS, "checkCurrentThread", "()V");
        }

        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(max(1, maxStack), maxLocals);
        }
    }
}
