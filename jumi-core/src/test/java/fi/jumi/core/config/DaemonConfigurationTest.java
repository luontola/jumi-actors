// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DaemonConfigurationTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();


    private DaemonConfigurationBuilder builder = new DaemonConfigurationBuilder();

    @Before
    public void setup() {
        builder.launcherPort(new Random().nextInt(100) + 1);

        // make sure that melting makes all fields back mutable
        builder = builder.freeze().melt();
    }


    // command line arguments

    @Test
    public void launcher_port_is_configurable() {
        builder.launcherPort(123);

        assertThat(configuration().launcherPort(), is(123));
    }

    @Test
    public void launcher_port_is_required() {
        int uninitializedValue = new DaemonConfiguration().launcherPort();
        builder.launcherPort(uninitializedValue);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("missing required parameter: " + DaemonConfigurationConverter.LAUNCHER_PORT);
        configuration();
    }

    @Test
    public void rejects_unsupported_command_line_arguments() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported parameter: --foo");
        parseArgs("--foo");
    }


    // system properties

    @Test
    public void logging_actor_messages_can_be_enabled() {
        builder.logActorMessages(true);

        assertThat(configuration().logActorMessages(), is(true));
    }

    @Test
    public void logging_actor_messages_defaults_to_disabled() {
        assertThat(configuration().logActorMessages(), is(false));
    }

    @Test
    public void startup_timeout_can_be_changed() {
        builder.startupTimeout(42L);

        assertThat(configuration().startupTimeout(), is(42L));
    }

    @Test
    public void startup_timeout_has_a_default_value() {
        assertThat(configuration().startupTimeout(), is(DaemonConfiguration.DEFAULT_STARTUP_TIMEOUT));
    }

    @Test
    public void idle_timeout_can_be_changed() {
        builder.idleTimeout(42L);

        assertThat(configuration().idleTimeout(), is(42L));
    }

    @Test
    public void idle_timeout_has_a_default_value() {
        assertThat(configuration().idleTimeout(), is(DaemonConfiguration.DEFAULT_IDLE_TIMEOUT));
    }

    @Test
    public void no_system_properties_are_produced_for_parameters_at_their_default_values() {
        DaemonConfiguration defaultValues = builder.freeze();

        Map<String, String> systemProperties = defaultValues.toSystemProperties();

        assertThat(systemProperties).isEmpty();
    }


    // helpers

    private DaemonConfiguration configuration() {
        DaemonConfiguration config = builder.freeze();
        Map<String, String> systemProperties = config.toSystemProperties();
        String[] args = config.toProgramArgs();
        return DaemonConfigurationConverter.parse(args, toProperties(systemProperties));
    }

    private static Properties toProperties(Map<String, String> map) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    private DaemonConfiguration parseArgs(String... args) {
        return DaemonConfigurationConverter.parse(args, new Properties());
    }
}
