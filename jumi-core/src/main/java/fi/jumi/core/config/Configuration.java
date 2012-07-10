// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class Configuration {

    public static final String LAUNCHER_PORT = "--launcher-port";

    public int launcherPort;

    public static Configuration parse(String[] args) {
        Configuration config = new Configuration();
        parseCommandLineArguments(config, args);
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
}
