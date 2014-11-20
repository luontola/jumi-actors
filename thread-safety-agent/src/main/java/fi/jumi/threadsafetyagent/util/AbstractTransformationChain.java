// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent.util;

import org.objectweb.asm.*;

import java.lang.instrument.*;
import java.security.ProtectionDomain;

public abstract class AbstractTransformationChain implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // TODO: at least the ClassLoader could be passed to the adapters, so they could examine super classes, package annotations etc. 
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw;
        if (enableAdditiveTransformationOptimization()) {
            cw = new ClassWriter(cr, 0);
        } else {
            cw = new ClassWriter(0);
        }
        try {
            ClassVisitor cv = getAdapters(cw);
            cr.accept(cv, 0);
            return cw.toByteArray();
        } catch (DoNotTransformException e) {
            return null;
        }
    }

    protected boolean enableAdditiveTransformationOptimization() {
        return true;
    }

    protected abstract ClassVisitor getAdapters(ClassVisitor cv);
}
