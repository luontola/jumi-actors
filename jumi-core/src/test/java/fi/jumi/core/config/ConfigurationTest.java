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
        String[] args = new String[]{Configuration.LAUNCHER_PORT, "123"};

        Configuration config = Configuration.parse(args);

        assertThat(config.launcherPort, is(123));
    }

    @Test
    public void launcher_port_is_required() {
        String[] args = new String[]{};

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("missing required parameter: " + Configuration.LAUNCHER_PORT);
        Configuration.parse(args);
    }

    @Test
    public void rejects_unsupported_command_line_arguments() {
        String[] args = new String[]{"--foo"};

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported parameter: --foo");
        Configuration.parse(args);
    }
}
