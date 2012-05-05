// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.core.SystemProperties;

public class TestSystemProperties {

    public static final String USE_THREAD_SAFETY_AGENT = "jumi.useThreadSafetyAgent";

    public static boolean useThreadSafetyAgent() {
        return SystemProperties.isTrue(USE_THREAD_SAFETY_AGENT);
    }
}
