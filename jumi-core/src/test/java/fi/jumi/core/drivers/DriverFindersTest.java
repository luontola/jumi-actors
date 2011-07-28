// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.RunVia;
import fi.jumi.api.drivers.*;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;

public class DriverFindersTest {

    @Test
    public void finds_driver_from_RunVia_annotation() {
        assertEquals(DummyDriver.class, findDriverFor(RunViaAnnotatedClass.class));
    }

    private Class<? extends Driver> findDriverFor(Class<RunViaAnnotatedClass> viaAnnotatedClassClass) {
        // TODO: support more driver finders: @RunWith, JUnit 4 (@Test), JUnit 3 (TestCase)
        DriverFinder finder = new RunViaAnnotationDriverFinder();
        return finder.findTestClassDriver(viaAnnotatedClassClass);
    }

    @RunVia(DummyDriver.class)
    private class RunViaAnnotatedClass {
    }

    private class DummyDriver implements Driver {
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }
}
