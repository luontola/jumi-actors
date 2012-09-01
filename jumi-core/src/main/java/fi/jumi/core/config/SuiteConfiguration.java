// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import fi.jumi.core.util.Immutables;

import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.util.*;

@Immutable
public class SuiteConfiguration implements Serializable {

    public static final String DEFAULT_INCLUDED_TESTS_PATTERN = "<TODO>"; // TODO

    // TODO: support for main and test class paths
    private final List<File> classPath;
    private final List<String> jvmOptions;
    private final String includedTestsPattern;

    public SuiteConfiguration() {
        classPath = Collections.emptyList();
        jvmOptions = Collections.emptyList();
        includedTestsPattern = DEFAULT_INCLUDED_TESTS_PATTERN;
    }

    SuiteConfiguration(SuiteConfigurationBuilder builder) {
        classPath = Immutables.list(builder.classPath());
        jvmOptions = Immutables.list(builder.jvmOptions());
        includedTestsPattern = builder.includedTestsPattern();
    }

    public SuiteConfigurationBuilder melt() {
        return new SuiteConfigurationBuilder(this);
    }


    // getters

    public List<File> classPath() {
        return classPath;
    }

    public List<String> jvmOptions() {
        return jvmOptions;
    }

    public String includedTestsPattern() {
        return includedTestsPattern;
    }
}
