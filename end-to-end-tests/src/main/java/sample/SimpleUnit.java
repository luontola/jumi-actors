// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import net.orfjackal.jumi.api.drivers.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;

public class SimpleUnit implements Driver {
    public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());

        List<Method> testMethods = getTestMethods(testClass);
        if (testMethods.size() == 0) {
            // TODO
//            TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
//            tn.fireFailure(new AssertionError("No tests found from " + testClass));
//            tn.fireTestFinished();
        }

        TestId testMethodId = TestId.ROOT.getFirstChild();
        for (Method testMethod : testMethods) {
            notifier.fireTestFound(testMethodId, testMethod.getName());
            executor.execute(new RunTestMethod(testMethod, testMethodId));
            testMethodId = testMethodId.nextSibling();
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

    private static class RunTestMethod implements Runnable {
        private final Method testMethod;
        private final TestId testMethodId;

        public RunTestMethod(Method testMethod, TestId testMethodId) {
            this.testMethod = testMethod;
            this.testMethodId = testMethodId;
        }

        public void run() {
            Class<?> testClass = testMethod.getDeclaringClass();
            // TODO
            // - fire test started
            // - create an instance of test class
            // - call the test method
            // - fire test finished
        }
    }
}
