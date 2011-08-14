// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import fi.jumi.threadsafetyagent.util.*;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.*;

import javax.annotation.concurrent.*;
import java.lang.instrument.ClassFileTransformer;

import static fi.jumi.threadsafetyagent.ThreadUtil.runInNewThread;

public class AddThreadSafetyChecksTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void experiment() throws Exception {
        // TODO: remove me
        ASMifierClassVisitor.main(new String[]{InterfaceAnnotatedNotThreadSafe.class.getName()});
    }

    @Test
    public void reference_implementation_checks_current_thread() throws Throwable {
        Runnable target = new ReferenceImplementation();
        assertChecksThreadSafety(target);
    }

    @Test
    public void classes_annotated_NotThreadSafe_are_transformed() throws Throwable {
        Runnable target = (Runnable) newInstrumentedInstance(NotThreadSafeClass.class);
        assertChecksThreadSafety(target);
    }

    @Test
    public void non_annotated_and_thread_safe_classes_are_not_transformed() throws Throwable {
        assertDoesNotCheckThreadSafety((Runnable) newInstrumentedInstance(NonAnnotatedClass.class));
        assertDoesNotCheckThreadSafety((Runnable) newInstrumentedInstance(ThreadSafeClass.class));
        assertDoesNotCheckThreadSafety((Runnable) newInstrumentedInstance(ImmutableClass.class));
    }

    @Test
    public void interfaces_are_not_transformed() throws Exception {
        instrumentClass(InterfaceAnnotatedNotThreadSafe.class);
    }

    @Test
    public void static_methods_are_not_transformed() throws Exception {
        Class<?> clazz = instrumentClass(NotThreadSafeClassWithStaticMethods.class);
        clazz.getMethod("staticMethod").invoke(null);
    }


    // helpers

    private void assertChecksThreadSafety(Runnable target) throws Throwable {
        runInNewThread("T1", target);
        thrown.expect(AssertionError.class);
        runInNewThread("T2", target);
    }

    private void assertDoesNotCheckThreadSafety(Runnable target) throws Throwable {
        runInNewThread("T1", target);
        runInNewThread("T2", target);
    }

    private static Object newInstrumentedInstance(Class<?> cls) throws Exception {
        return instrumentClass(cls).newInstance();
    }

    private static Class<?> instrumentClass(Class<?> cls) throws Exception {
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

    @NotThreadSafe
    public static interface InterfaceAnnotatedNotThreadSafe {
        void shouldNotAddCodeToThisMethod();
    }

    @NotThreadSafe
    public static class NotThreadSafeClassWithStaticMethods {
        public static void staticMethod() {
            // should not add code to this method
        }
    }
}
