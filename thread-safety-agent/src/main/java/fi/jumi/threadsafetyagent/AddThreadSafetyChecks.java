// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
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

    public AddThreadSafetyChecks(ClassVisitor next) {
        super(Opcodes.ASM5, next);
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
        MethodVisitor next = super.visitMethod(access, name, desc, signature, exceptions);
        if (isConstructor(name)) {
            next = new InstantiateChecker(next);
        } else if (isInstanceMethod(access)) {
            next = new CallChecker(next);
        }
        return next;
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

        public InstantiateChecker(MethodVisitor next) {
            super(Opcodes.ASM5, next);
        }

        @Override
        public void visitInsn(int opcode) {
            // FIXME: handle all the xRETURN opcodes
            if (opcode == RETURN) {
                // insert to the end of the method
                super.visitVarInsn(ALOAD, 0);
                super.visitTypeInsn(NEW, CHECKER_CLASS);
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESPECIAL, CHECKER_CLASS, "<init>", "()V", false);
                super.visitFieldInsn(PUTFIELD, myClassName, CHECKER_FIELD, CHECKER_CLASS_DESC);
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // XXX: stack might not be empty right before a RETURN statement, so this maxStack can be too optimistic
            super.visitMaxs(max(3, maxStack), maxLocals);
        }
    }

    private class CallChecker extends MethodVisitor {

        public CallChecker(MethodVisitor next) {
            super(Opcodes.ASM5, next);
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
            super.visitMethodInsn(INVOKEVIRTUAL, CHECKER_CLASS, "checkCurrentThread", "()V", false);
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
