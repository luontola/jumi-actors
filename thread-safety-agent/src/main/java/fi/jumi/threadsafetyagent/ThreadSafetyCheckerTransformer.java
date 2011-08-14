// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import fi.jumi.threadsafetyagent.util.AbstractTransformationChain;
import org.objectweb.asm.ClassVisitor;

public class ThreadSafetyCheckerTransformer extends AbstractTransformationChain {

    protected ClassVisitor getAdapters(ClassVisitor cv) {
        // the adapter declared last is processed first
        cv = new AddThreadSafetyChecks(cv);
        cv = new EnabledWhenAnnotatedWith("javax/annotation/concurrent/NotThreadSafe", cv);
        return cv;
    }
}
