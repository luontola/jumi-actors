// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.Immutable;
import java.util.*;

@Immutable
public class DaemonConfigurationConverter {

    // command line arguments
    public static final String LAUNCHER_PORT = "--launcher-port";

    // system properties
    public static final String LOG_ACTOR_MESSAGES = "jumi.daemon.logActorMessages";
    public static final String STARTUP_TIMEOUT = "jumi.daemon.startupTimeout";
    public static final String IDLE_TIMEOUT = "jumi.daemon.idleTimeout";

    // TODO: create a generic way of representing the properties?

    public static Map<String, String> toSystemProperties(DaemonConfiguration config) {
        Map<String, String> map = new HashMap<String, String>();
        if (config.logActorMessages() != DaemonConfiguration.DEFAULT_LOG_ACTOR_MESSAGES) {
            map.put(LOG_ACTOR_MESSAGES, "" + config.logActorMessages());
        }
        if (config.startupTimeout() != DaemonConfiguration.DEFAULT_STARTUP_TIMEOUT) {
            map.put(STARTUP_TIMEOUT, String.valueOf(config.startupTimeout()));
        }
        if (config.idleTimeout() != DaemonConfiguration.DEFAULT_IDLE_TIMEOUT) {
            map.put(IDLE_TIMEOUT, String.valueOf(config.idleTimeout()));
        }
        return map;
    }

    public static DaemonConfiguration parse(String[] args, Properties systemProperties) {
        DaemonConfigurationBuilder builder = new DaemonConfigurationBuilder();
        parseCommandLineArguments(builder, args);
        parseSystemProperties(builder, systemProperties);
        checkRequiredParameters(builder);
        return builder.build();
    }

    private static void parseCommandLineArguments(DaemonConfigurationBuilder builder, String[] args) {
        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String parameter = it.next();
            if (parameter.equals(LAUNCHER_PORT)) {
                builder.launcherPort(Integer.parseInt(it.next()));
            } else {
                throw new IllegalArgumentException("unsupported parameter: " + parameter);
            }
        }
    }

    private static void parseSystemProperties(DaemonConfigurationBuilder builder, Properties systemProperties) {
        builder.logActorMessages(getBoolean(LOG_ACTOR_MESSAGES, systemProperties));
        builder.startupTimeout(getLong(STARTUP_TIMEOUT, systemProperties, DaemonConfiguration.DEFAULT_STARTUP_TIMEOUT));
        builder.idleTimeout(getLong(IDLE_TIMEOUT, systemProperties, DaemonConfiguration.DEFAULT_IDLE_TIMEOUT));
    }

    private static void checkRequiredParameters(DaemonConfigurationBuilder builder) {
        if (builder.launcherPort() <= 0) {
            throw new IllegalArgumentException("missing required parameter: " + LAUNCHER_PORT);
        }
    }

    private static boolean getBoolean(String key, Properties properties) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    private static long getLong(String key, Properties properties, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }
}
