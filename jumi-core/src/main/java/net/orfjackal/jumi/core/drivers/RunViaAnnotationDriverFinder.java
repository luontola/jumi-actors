// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.drivers;

import net.orfjackal.jumi.api.RunVia;
import net.orfjackal.jumi.api.drivers.Driver;

public class RunViaAnnotationDriverFinder implements DriverFinder {

    public Class<? extends Driver> findTestClassDriver(Class<?> testClass) {
        RunVia runVia = testClass.getAnnotation(RunVia.class);
        return runVia.value();
    }
}
