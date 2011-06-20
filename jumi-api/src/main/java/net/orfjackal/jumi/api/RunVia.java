// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.api;

import net.orfjackal.jumi.api.drivers.Driver;

/**
 * Marks a class as a test and tells that which {@link Driver} to use for running the test.
 */
public @interface RunVia {

    Class<? extends Driver> value();
}
