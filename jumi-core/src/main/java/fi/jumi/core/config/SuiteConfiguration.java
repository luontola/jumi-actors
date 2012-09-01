// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import fi.jumi.core.util.Immutables;

import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.util.List;

@Immutable
public class SuiteConfiguration {

    private final List<File> classPath;
    private final List<String> jvmOptions;
    private final String includedTestsPattern;

    SuiteConfiguration(SuiteConfigurationBuilder builder) {
        classPath = Immutables.list(builder.classPath());
        jvmOptions = Immutables.list(builder.jvmOptions());
        includedTestsPattern = builder.includedTestsPattern();
    }

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
