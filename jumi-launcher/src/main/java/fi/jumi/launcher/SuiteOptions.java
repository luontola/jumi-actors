// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import fi.jumi.core.config.Configuration;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.util.*;

@NotThreadSafe
public class SuiteOptions {

    // TODO: support for main and test class paths

    public final List<File> classPath = new ArrayList<File>();
    public final List<String> jvmOptions = new ArrayList<String>();
    public final Properties systemProperties = new Properties();
    public String testsToIncludePattern;

    public SuiteOptions copy() { // XXX: remove me after making this class immutable
        SuiteOptions copy = new SuiteOptions();
        copy.classPath.addAll(this.classPath);
        copy.jvmOptions.addAll(this.jvmOptions);
        for (Map.Entry<Object, Object> entry : this.systemProperties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            copy.systemProperties.setProperty(key, value);
        }
        copy.testsToIncludePattern = this.testsToIncludePattern;
        return copy;
    }

    public void addToClassPath(File file) {
        classPath.add(file);
    }

    public void setTestsToInclude(String pattern) {
        testsToIncludePattern = pattern;
    }

    public void addJvmOptions(String... jvmOptions) {
        this.jvmOptions.addAll(Arrays.asList(jvmOptions));
    }

    public void enableMessageLogging() {
        systemProperties.setProperty(Configuration.LOG_ACTOR_MESSAGES, "true");
    }

    public void setStartupTimeout(long startupTimeout) {
        systemProperties.setProperty(Configuration.STARTUP_TIMEOUT, String.valueOf(startupTimeout));
    }

    public void setIdleTimeout(long idleTimeout) {
        systemProperties.setProperty(Configuration.IDLE_TIMEOUT, String.valueOf(idleTimeout));
    }
}
