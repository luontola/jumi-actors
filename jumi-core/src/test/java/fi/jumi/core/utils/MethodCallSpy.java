// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.utils;

import java.lang.reflect.*;
import java.util.*;

public class MethodCallSpy implements InvocationHandler {

    public final List<String> methodCalls = new ArrayList<String>();

    public <T> T createProxyTo(Class<T> anInterface) {
        return anInterface.cast(Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{anInterface},
                this
        ));
    }

    public int countCallsTo(String methodName) {
        int count = 0;
        for (String s : methodCalls) {
            if (s.equals(methodName)) {
                count++;
            }
        }
        return count;
    }

    public String getLastCall() {
        return methodCalls.get(methodCalls.size() - 1);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        methodCalls.add(method.getName());
        return null;
    }
}
