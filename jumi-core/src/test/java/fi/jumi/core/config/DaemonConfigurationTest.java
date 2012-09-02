// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DaemonConfigurationTest {

    private static final long ONE_SECOND = 1000L;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private DaemonConfigurationBuilder builder = new DaemonConfigurationBuilder();

    @Before
    public void setup() {
        // initialize required parameters to avoid failing unrelated tests
        builder.launcherPort(new Random().nextInt(100) + 1);

        // make sure that melting makes all fields back mutable
        builder = builder.freeze().melt();
    }


    // ## Command Line Arguments ##

    @Test
    public void rejects_unsupported_command_line_arguments() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported parameter: --foo");
        builder.parseProgramArgs("--foo");
    }

    // launcherPort

    @Test
    public void launcher_port_is_configurable() {
        builder.launcherPort(123);

        assertThat(configuration().launcherPort(), is(123));
    }

    @Test
    public void launcher_port_is_required() {
        builder.launcherPort(DaemonConfiguration.DEFAULTS.launcherPort());

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("missing required parameter: " + DaemonConfiguration.LAUNCHER_PORT);
        configuration();
    }


    // ## System Properties ##

    @Test
    public void no_system_properties_are_produced_for_parameters_at_their_default_values() {
        DaemonConfiguration defaultValues = builder.freeze();

        Properties systemProperties = defaultValues.toSystemProperties();

        assertThat(systemProperties).isEmpty();
    }

    // logActorMessages

    @Test
    public void logging_actor_messages_can_be_enabled() {
        builder.logActorMessages(true);

        assertThat(configuration().logActorMessages(), is(true));
    }

    @Test
    public void logging_actor_messages_defaults_to_disabled() {
        assertThat(configuration().logActorMessages(), is(false));
    }

    // startupTimeout

    @Test
    public void startup_timeout_can_be_changed() {
        builder.startupTimeout(42L);

        assertThat(configuration().startupTimeout(), is(42L));
    }

    @Test
    public void startup_timeout_has_a_default_value() {
        assertThat(configuration().startupTimeout(), is(greaterThanOrEqualTo(ONE_SECOND)));
    }

    // idleTimeout

    @Test
    public void idle_timeout_can_be_changed() {
        builder.idleTimeout(42L);

        assertThat(configuration().idleTimeout(), is(42L));
    }

    @Test
    public void idle_timeout_has_a_default_value() {
        assertThat(configuration().idleTimeout(), is(greaterThanOrEqualTo(ONE_SECOND)));
    }


    // helpers

    private DaemonConfiguration configuration() {
        DaemonConfiguration config = builder.freeze();
        String[] args = config.toProgramArgs();
        Properties systemProperties = config.toSystemProperties();

        return new DaemonConfigurationBuilder()
                .parseProgramArgs(args)
                .parseSystemProperties(systemProperties)
                .freeze();
    }
}
