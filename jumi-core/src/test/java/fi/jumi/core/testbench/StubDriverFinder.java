// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.testbench;

import fi.jumi.api.drivers.Driver;
import fi.jumi.core.drivers.DriverFinder;

public class StubDriverFinder implements DriverFinder {

    private final Driver driver;

    public StubDriverFinder(Driver driver) {
        this.driver = driver;
    }

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        return driver;
    }
}
