// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.runner.*;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.*;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

public class PartiallyParameterized extends Parameterized {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface NonParameterized {
    }

    public PartiallyParameterized(Class<?> klass) throws Throwable {
        super(klass);
        List<String> nonParameterizedMethods = getNonParameterizedMethods(klass);
        List<Runner> runners = getChildren();
        for (int i = 1; i < runners.size(); i++) { // filter all but one
            BlockJUnit4ClassRunner runner = (BlockJUnit4ClassRunner) runners.get(i);
            runner.filter(new ExcludeNonParameterized(nonParameterizedMethods));
        }
    }

    private static List<String> getNonParameterizedMethods(Class<?> klass) {
        List<String> nonParameterizedMethods = new ArrayList<>();
        for (Method method : klass.getMethods()) {
            if (method.getAnnotation(NonParameterized.class) != null) {
                nonParameterizedMethods.add(method.getName());
            }
        }
        return nonParameterizedMethods;
    }

    private static class ExcludeNonParameterized extends Filter {

        private final List<String> nonParameterizedMethods;

        public ExcludeNonParameterized(List<String> nonParameterizedMethods) {
            this.nonParameterizedMethods = nonParameterizedMethods;
        }

        @Override
        public boolean shouldRun(Description description) {
            String methodName = description.getMethodName().replaceAll("\\[\\d+\\]$", "");
            return !nonParameterizedMethods.contains(methodName);
        }

        @Override
        public String describe() {
            return "exclude non parameterized";
        }
    }
}
