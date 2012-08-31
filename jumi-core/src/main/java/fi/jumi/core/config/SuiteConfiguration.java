// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import fi.jumi.core.util.Immutables;

import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.util.*;

@Immutable
public class SuiteConfiguration {

    private final List<File> classPath;
    private final List<String> jvmOptions;
    private final String testsToIncludePattern;

    private final boolean daemonMessageLogging;
    private final long daemonStartupTimeout;
    private final long daemonIdleTimeout;

    SuiteConfiguration(SuiteConfigurationBuilder builder) {
        classPath = Immutables.list(builder.getClassPath());
        jvmOptions = Immutables.list(builder.getJvmOptions());
        testsToIncludePattern = builder.getTestsToIncludePattern();

        // TODO: separate daemon configuration to its own class?
        daemonMessageLogging = builder.isDaemonMessageLogging();
        daemonStartupTimeout = builder.getDaemonStartupTimeout();
        daemonIdleTimeout = builder.getDaemonIdleTimeout();
    }

    public List<File> getClassPath() {
        return classPath;
    }

    public List<String> getJvmOptions() {
        return jvmOptions;
    }

    public String getTestsToIncludePattern() {
        return testsToIncludePattern;
    }

    public Map<String, String> toDaemonSystemProperties() {
        Map<String, String> map = new HashMap<String, String>();
        // TODO: don't set when default
        map.put(Configuration.LOG_ACTOR_MESSAGES, "" + daemonMessageLogging);
        map.put(Configuration.STARTUP_TIMEOUT, String.valueOf(daemonStartupTimeout));
        map.put(Configuration.IDLE_TIMEOUT, String.valueOf(daemonIdleTimeout));
        return map;
    }

    public boolean isDaemonMessageLogging() {
        return daemonMessageLogging;
    }

    public long getDaemonStartupTimeout() {
        return daemonStartupTimeout;
    }

    public long getDaemonIdleTimeout() {
        return daemonIdleTimeout;
    }
}
