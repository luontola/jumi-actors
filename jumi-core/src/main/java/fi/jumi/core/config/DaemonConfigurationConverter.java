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

    // TODO: generic way for representing command line arguments

    public static DaemonConfiguration parse(String[] args, Properties systemProperties) {
        DaemonConfigurationBuilder builder = new DaemonConfigurationBuilder();
        builder.parseSystemProperties(systemProperties);
        parseCommandLineArguments(builder, args);
        return builder.freeze();
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
        checkRequiredParameters(builder);
    }

    private static void checkRequiredParameters(DaemonConfigurationBuilder builder) {
        if (builder.launcherPort() <= 0) {
            throw new IllegalArgumentException("missing required parameter: " + LAUNCHER_PORT);
        }
    }
}
