// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import fi.jumi.threadsafetyagent.util.*;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import javax.annotation.concurrent.*;
import java.lang.instrument.ClassFileTransformer;

import static fi.jumi.threadsafetyagent.ThreadUtil.runInNewThread;

public class AddThreadSafetyChecksTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

//    @Test
//    public void experiment() throws Exception {
//        ASMifierClassVisitor.main(new String[] {"fi.jumi.threadsafetyagent.AddThreadSafetyChecksTest$ReferenceImplementation"});
//    }

    @Test
    public void reference_implementation_checks_current_thread() throws Throwable {
        Runnable target = new ReferenceImplementation();
        assertChecksThreadSafety(target);
    }

    @Test
    public void not_thread_safe_annotated_classes_are_checked() throws Throwable {
        Runnable target = (Runnable) newInstrumentedInstance(NotThreadSafeClass.class);
        assertChecksThreadSafety(target);
    }

    // TODO: ignore non-annotated
    // TODO: transforms non-thread-safe
    // TODO: ignore thread-safe & immutable


    // helpers

    private void assertChecksThreadSafety(Runnable target) throws Throwable {
        runInNewThread("T1", target);
        thrown.expect(AssertionError.class);
        runInNewThread("T2", target);
    }

    private static Object newInstrumentedInstance(Class<?> cls) throws Exception {
        return instrumentClass(cls).newInstance();
    }

    private static Class<?> instrumentClass(Class<?> cls) throws ClassNotFoundException {
        ClassFileTransformer transformer = new AbstractTransformationChain() {
            protected ClassVisitor getAdapters(ClassVisitor cv) {
                cv = new CheckClassAdapter(cv);
                cv = new AddThreadSafetyChecks(cv);
                return cv;
            }
        };
        ClassLoader loader = new TransformationTestClassLoader(cls.getName(), transformer);
        return loader.loadClass(cls.getName());
    }


    // guinea pigs

    public static class ReferenceImplementation implements Runnable {
        private final ThreadSafetyChecker checker = new ThreadSafetyChecker();

        public void run() {
            checker.checkCurrentThread();
        }
    }

    @NotThreadSafe
    public static class NotThreadSafeClass implements Runnable {
        public void run() {
        }
    }

    @ThreadSafe
    public static class ThreadSafeClass implements Runnable {
        public void run() {
        }
    }

    @Immutable
    public static class ImmutableClass implements Runnable {
        public void run() {
        }
    }

    public static class NonAnnotatedClass implements Runnable {
        public void run() {
        }
    }
}
