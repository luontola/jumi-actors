// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import fi.jumi.threadsafetyagent.util.TransformationTestClassLoader;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import javax.annotation.concurrent.*;
import java.lang.instrument.ClassFileTransformer;

import static fi.jumi.threadsafetyagent.ThreadUtil.runInNewThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AddThreadSafetyChecksTest {

    private static final String DUMMY_EXCEPTION = "dummy exception";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

//    @Test
//    public void experiment() throws Exception {
//        ASMifierClassVisitor.main(new String[]{"-debug", ReferenceImplementation.class.getName()});
//    }

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

    @Test
    public void stack_trace_for_generated_code_contains_a_line_number() throws Throwable {
        Runnable instrumented = (Runnable) newInstrumentedInstance(NotThreadSafeClass.class);
        int generatedLine = getThreadSafetyCheckerExceptionLineNumber(instrumented);

        assertThat(generatedLine, is(greaterThanOrEqualTo(1)));
    }

    @Test
    public void line_number_for_generated_bytecode_is_the_first_line_of_the_method() throws Exception {
        int secondLine = getDummyExceptionLineNumber(new ThrowExceptionOnSecondLine());
        int firstLine = secondLine - 1;

        Runnable instrumented = (Runnable) newInstrumentedInstance(ThrowExceptionOnSecondLine.class);
        int generatedLine = getThreadSafetyCheckerExceptionLineNumber(instrumented);

        assertThat(generatedLine, is(firstLine));
    }


    // bytecode generation helpers

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
        ClassFileTransformer transformer = new ThreadSafetyCheckerTransformer() {
            protected ClassVisitor getAdapters(ClassVisitor cv) {
                return super.getAdapters(new CheckClassAdapter(cv));
            }
        };
        ClassLoader loader = new TransformationTestClassLoader(cls.getName(), transformer, null);
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


    // line number helpers

    private static int getDummyExceptionLineNumber(Runnable notIstrumented) {
        Throwable t = getDummyExceptionThrownBy(notIstrumented);
        return getLineNumber(notIstrumented.getClass(), "run", t);
    }

    private static int getThreadSafetyCheckerExceptionLineNumber(Runnable instrumented) {
        Throwable t = getThreadSafetyException(instrumented);
        return getLineNumber(instrumented.getClass(), "run", t.getCause());
    }

    private static Throwable getDummyExceptionThrownBy(Runnable notIstrumented) { // TODO: remove duplication?
        try {
            notIstrumented.run();
            return null;
        } catch (Exception e) {
            assertThat("class should NOT have been instrumented", e.getMessage(), is(DUMMY_EXCEPTION));
            return e;
        }
    }

    private static Throwable getThreadSafetyException(Runnable instrumented) {
        try {
            runInNewThread("T1", instrumented);
        } catch (Throwable throwable) {
            // ignore any exception naturally thrown by the method
        }
        try {
            runInNewThread("T2", instrumented);
            return null;
        } catch (Throwable t) {
            assertThat("class should have been instrumented", t.getMessage(), is(not(DUMMY_EXCEPTION)));
            return t;
        }
    }

    private static int getLineNumber(Class<?> clazz, String methodName, Throwable t) {
        for (StackTraceElement stackFrame : t.getStackTrace()) {
            if (stackFrame.getClassName().equals(clazz.getName())
                    && stackFrame.getMethodName().equals(methodName)) {
                return stackFrame.getLineNumber();
            }
        }
        throw new IllegalArgumentException("stack trace did not contain calls to method " + clazz.getName() + "." + methodName, t);
    }

    @NotThreadSafe
    public static class ThrowExceptionOnSecondLine implements Runnable {
        public void run() {
            // Dummy line, to make the exception throw to be the second line.
            // This tests against duplicate line number entries, in which case
            // the JVM would apparently use the last matching entry in the line number table.
            Thread.yield();
            throw new RuntimeException(DUMMY_EXCEPTION);
        }
    }
}
