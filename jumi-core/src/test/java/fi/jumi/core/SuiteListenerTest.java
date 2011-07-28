// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;

public class SuiteListenerTest {

    /**
     * The {@link SuiteListener} interface is used for events sent over the network
     * from daemon to launcher. It must not contain any Class instances as parameters,
     * because the receiver might not have those classes on its classpath.
     */
    @Test
    public void event_parameters_of_type_javaLangClass_are_not_allowed() {
        assertTakesNoParametersOfTypeClass(SuiteListener.class);
    }

    private static void assertTakesNoParametersOfTypeClass(Class<?> subject) {
        for (Method method : subject.getMethods()) {
            for (Class<?> parameterType : method.getParameterTypes()) {
                assertFalse("method " + method + " had forbidden parameter type " + parameterType, parameterType.equals(Class.class));
            }
        }
    }
}
