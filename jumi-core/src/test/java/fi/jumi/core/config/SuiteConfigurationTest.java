// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SuiteConfigurationTest {

    private final SuiteConfigurationBuilder builder = new SuiteConfigurationBuilder();


    // classPath

    @Test
    public void class_path_can_be_changed() {
        builder.addToClassPath(new File("foo.jar"));

        assertThat(configuration().classPath(), contains(new File("foo.jar")));
    }

    @Test
    public void class_path_defaults_to_empty() {
        assertThat(configuration().classPath(), is(empty()));
    }


    // jvmOptions

    @Test
    public void jvm_options_can_be_changed() {
        builder.addJvmOptions("-option");

        assertThat(configuration().jvmOptions(), contains("-option"));
    }

    @Test
    public void jvm_options_defaults_to_empty() {
        assertThat(configuration().jvmOptions(), is(empty()));
    }


    // includedTestsPattern

    @Test
    public void included_tests_pattern_can_be_changed() {
        builder.includedTestsPattern("the pattern");

        assertThat(configuration().includedTestsPattern(), is("the pattern"));
    }

    @Test
    public void included_tests_pattern_has_a_default_value() {
        assertThat(configuration().includedTestsPattern(), is(SuiteConfigurationBuilder.DEFAULT_INCLUDED_TESTS_PATTERN));
    }


    // helpers

    private SuiteConfiguration configuration() {
        return builder.build();
    }
}
