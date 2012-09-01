// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.util.*;

@NotThreadSafe
public class SuiteConfigurationBuilder {

    public static final String DEFAULT_INCLUDED_TESTS_PATTERN = "<TODO>"; // TODO

    // TODO: support for main and test class paths

    private final List<File> classPath = new ArrayList<File>();
    private final List<String> jvmOptions = new ArrayList<String>();
    private String includedTestsPattern = DEFAULT_INCLUDED_TESTS_PATTERN;

    public List<File> classPath() {
        return classPath;
    }

    public SuiteConfigurationBuilder addToClassPath(File file) {
        classPath.add(file);
        return this;
    }

    public String includedTestsPattern() {
        return includedTestsPattern;
    }

    public SuiteConfigurationBuilder includedTestsPattern(String includedTestsPattern) {
        this.includedTestsPattern = includedTestsPattern;
        return this;
    }

    public List<String> jvmOptions() {
        return jvmOptions;
    }

    public SuiteConfigurationBuilder addJvmOptions(String... jvmOptions) {
        this.jvmOptions.addAll(Arrays.asList(jvmOptions));
        return this;
    }

    public SuiteConfiguration build() {
        return new SuiteConfiguration(this);
    }
}
