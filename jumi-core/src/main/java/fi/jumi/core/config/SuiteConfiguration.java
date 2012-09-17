// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import fi.jumi.core.util.Immutables;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.net.URI;
import java.util.*;

@Immutable
public class SuiteConfiguration implements Serializable {

    public static final SuiteConfiguration DEFAULTS = new SuiteConfiguration();

    // TODO: support for main and test class paths
    private final List<URI> classPath;
    private final List<String> jvmOptions;
    private final String testClass;

    public SuiteConfiguration() {
        classPath = Collections.emptyList();
        jvmOptions = Collections.emptyList();
        testClass = ""; // TODO
    }

    SuiteConfiguration(SuiteConfigurationBuilder src) {
        classPath = Immutables.list(src.classPath());
        jvmOptions = Immutables.list(src.jvmOptions());
        testClass = src.testClass();
    }

    public SuiteConfigurationBuilder melt() {
        return new SuiteConfigurationBuilder(this);
    }


    // getters

    public List<URI> classPath() {
        return classPath;
    }

    public List<String> jvmOptions() {
        return jvmOptions;
    }

    public String testClass() {
        return testClass;
    }
}
