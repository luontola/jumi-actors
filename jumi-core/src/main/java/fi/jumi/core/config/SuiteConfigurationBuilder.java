// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.util.*;

@NotThreadSafe
public class SuiteConfigurationBuilder {

    // TODO: support for main and test class paths

    private final List<File> classPath = new ArrayList<File>();
    private final List<String> jvmOptions = new ArrayList<String>();
    private String testsToIncludePattern;

    private boolean daemonMessageLogging = Configuration.DEFAULT_MESSAGE_LOGGING;
    private long daemonStartupTimeout = Configuration.DEFAULT_STARTUP_TIMEOUT;
    private long daemonIdleTimeout = Configuration.DEFAULT_IDLE_TIMEOUT;

    public List<File> getClassPath() {
        return classPath;
    }

    public SuiteConfigurationBuilder addToClassPath(File file) {
        classPath.add(file);
        return this;
    }

    public String getTestsToIncludePattern() {
        return testsToIncludePattern;
    }

    public SuiteConfigurationBuilder setTestsToIncludePattern(String pattern) {
        testsToIncludePattern = pattern;
        return this;
    }

    public List<String> getJvmOptions() {
        return jvmOptions;
    }

    public SuiteConfigurationBuilder addJvmOptions(String... jvmOptions) {
        this.jvmOptions.addAll(Arrays.asList(jvmOptions));
        return this;
    }

    public boolean isDaemonMessageLogging() {
        return daemonMessageLogging;
    }

    public SuiteConfigurationBuilder enableMessageLogging() {
        daemonMessageLogging = true;
        return this;
    }

    public long getDaemonStartupTimeout() {
        return daemonStartupTimeout;
    }

    public SuiteConfigurationBuilder setStartupTimeout(long startupTimeout) {
        daemonStartupTimeout = startupTimeout;
        return this;
    }

    public long getDaemonIdleTimeout() {
        return daemonIdleTimeout;
    }

    public SuiteConfigurationBuilder setIdleTimeout(long idleTimeout) {
        daemonIdleTimeout = idleTimeout;
        return this;
    }

    public SuiteConfiguration build() {
        return new SuiteConfiguration(this);
    }
}
