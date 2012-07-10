// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConfigurationTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void launcher_port_is_configurable() {
        Configuration config = parseArgs(Configuration.LAUNCHER_PORT, "123");

        assertThat(config.launcherPort, is(123));
    }

    @Test
    public void launcher_port_is_required() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("missing required parameter: " + Configuration.LAUNCHER_PORT);
        parseArgs();
    }

    @Test
    public void rejects_unsupported_command_line_arguments() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported parameter: --foo");
        parseArgs("--foo");
    }


    // helpers

    private Configuration parseArgs(String... args) {
        return Configuration.parse(args);
    }
}
