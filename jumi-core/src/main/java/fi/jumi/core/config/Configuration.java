// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class Configuration {

    // TODO: a generic way of representing property names and their types?

    // command line arguments
    public static final String LAUNCHER_PORT = "--launcher-port";

    // system properties
    public static final String LOG_ACTOR_MESSAGES = "jumi.logActorMessages";

    public int launcherPort;
    public boolean logActorMessages;

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
    }

    private static boolean getBoolean(String key, Properties properties) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }
}
