// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import fi.jumi.actors.generator.codegen.JavaType;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TargetPackageResolverTest {

    private static final String TOP_LEVEL_PACKAGE = "com.example.toplevel";

    @Test
    public void factories_go_to_the_top_level_package() {
        TargetPackageResolver resolver = new TargetPackageResolver(TOP_LEVEL_PACKAGE);

        assertThat(resolver.getEventizerPackage(), is(TOP_LEVEL_PACKAGE));
    }

    @Test
    public void stubs_go_to_a_sub_package_which_is_named_after_the_event_interface() {
        TargetPackageResolver resolver = new TargetPackageResolver(TOP_LEVEL_PACKAGE);
        JavaType eventInterface = JavaType.of(DummyListener.class);

        assertThat(resolver.getStubsPackage(eventInterface), is(TOP_LEVEL_PACKAGE + ".dummyListener"));
    }
}
