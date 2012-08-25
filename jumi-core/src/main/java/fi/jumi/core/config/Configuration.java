// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.concurrent.TimeUnit;

@NotThreadSafe
public class Configuration {

    // TODO: a generic way of representing property names and their types?

    // command line arguments
    public static final String LAUNCHER_PORT = "--launcher-port";

    // system properties
    public static final String LOG_ACTOR_MESSAGES = "jumi.logActorMessages";
    public static final String IDLE_TIMEOUT = "jumi.idleTimeout";

    // default values
    public static final long DEFAULT_IDLE_TIMEOUT = TimeUnit.SECONDS.toMillis(5); // TODO: increase to 15 min, after implementing persistent daemons

    public int launcherPort;
    public boolean logActorMessages;
    public long idleTimeout;

    public static Configuration parse(String[] args, Properties systemProperties) {
        Configuration config = new Configuration();
        parseCommandLineArguments(config, args);
        parseSystemProperties(config, systemProperties);
        checkRequiredParameters(config);
        return config;
    }

    private static void parseCommandLineArguments(Configuration config, String[] args) {
        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String parameter = it.next();
            if (parameter.equals(LAUNCHER_PORT)) {
                config.launcherPort = Integer.parseInt(it.next());
            } else {
                throw new IllegalArgumentException("unsupported parameter: " + parameter);
            }
        }
    }

    private static void checkRequiredParameters(Configuration config) {
        if (config.launcherPort <= 0) {
            throw new IllegalArgumentException("missing required parameter: " + Configuration.LAUNCHER_PORT);
        }
    }

    private static void parseSystemProperties(Configuration config, Properties systemProperties) {
        config.logActorMessages = getBoolean(LOG_ACTOR_MESSAGES, systemProperties);
        config.idleTimeout = getLong(IDLE_TIMEOUT, systemProperties, DEFAULT_IDLE_TIMEOUT);
    }

    private static long getLong(String key, Properties properties, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    private static boolean getBoolean(String key, Properties properties) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }
}
