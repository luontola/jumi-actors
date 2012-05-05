// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import javax.annotation.concurrent.Immutable;

@Immutable
public class SystemProperties {

    public static final String LOG_ACTOR_MESSAGES = "jumi.logActorMessages";

    public static boolean logActorMessages() {
        return isTrue(LOG_ACTOR_MESSAGES);
    }

    public static boolean isTrue(String property) {
        return Boolean.parseBoolean(System.getProperty(property, "false"));
    }
}
