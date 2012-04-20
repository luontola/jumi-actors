// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.simpleunit;

import fi.jumi.api.drivers.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SimpleUnit implements Driver {

    @Override
    public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());

        List<Method> testMethods = getTestMethods(testClass);
        if (testMethods.size() == 0) {
            TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
            tn.fireFailure(new IllegalArgumentException("No test methods in " + testClass));
            tn.fireTestFinished();
        }

        TestId testMethodId = TestId.ROOT.getFirstChild();
        for (Method testMethod : testMethods) {
            notifier.fireTestFound(testMethodId, testMethod.getName());
            executor.execute(new RunTestMethod(testMethod, testMethodId, notifier));
            testMethodId = testMethodId.getNextSibling();
        }
    }

    private static List<Method> getTestMethods(Class<?> testClass) {
        List<Method> results = new ArrayList<Method>();
        for (Method method : testClass.getMethods()) {
            if (method.getName().startsWith("test")) {
                results.add(method);
            }
        }
        return results;
    }


    @NotThreadSafe
    private static class RunTestMethod implements Runnable {
        private final Method testMethod;
        private final TestId testMethodId;
        private final SuiteNotifier notifier;

        public RunTestMethod(Method testMethod, TestId testMethodId, SuiteNotifier notifier) {
            this.testMethod = testMethod;
            this.testMethodId = testMethodId;
            this.notifier = notifier;
        }

        @Override
        public void run() {
            TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
            try {
                Object instance = testMethod.getDeclaringClass().newInstance();
                invokeTestMethodOn(instance);

            } catch (Throwable t) {
                tn.fireFailure(t);
            } finally {
                tn.fireTestFinished();
            }
        }

        private void invokeTestMethodOn(Object instance) {
            TestNotifier tn = notifier.fireTestStarted(testMethodId);
            try {
                testMethod.invoke(instance);

            } catch (InvocationTargetException e) {
                tn.fireFailure(e.getTargetException());
            } catch (Throwable t) {
                tn.fireFailure(t);
            } finally {
                tn.fireTestFinished();
            }
        }
    }
}
