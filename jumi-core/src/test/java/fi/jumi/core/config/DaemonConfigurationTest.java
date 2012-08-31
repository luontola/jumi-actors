// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DaemonConfigurationTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();


    // command line arguments

    @Test
    public void launcher_port_is_configurable() {
        // TODO: remove duplication with fi.jumi.launcher.remote.ProcessStartingDaemonSummoner.connectToDaemon()
        DaemonConfiguration config = parseArgs(DaemonConfigurationConverter.LAUNCHER_PORT, "123");

        assertThat(config.launcherPort(), is(123));
    }

    @Test
    public void launcher_port_is_required() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("missing required parameter: " + DaemonConfigurationConverter.LAUNCHER_PORT);
        parseArgs();
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
        DaemonConfiguration config = parseSystemProperties(DaemonConfigurationConverter.LOG_ACTOR_MESSAGES, "true");

        assertThat(config.logActorMessages(), is(true));
    }

    @Test
    public void logging_actor_messages_defaults_to_disabled() {
        DaemonConfiguration config = defaultConfig();

        assertThat(config.logActorMessages(), is(false));
    }

    @Test
    public void startup_timeout_can_be_changed() {
        DaemonConfiguration original = new DaemonConfigurationBuilder()
                .startupTimeout(42L)
                .build();

        DaemonConfiguration config = parseSuiteOptions(original);

        assertThat(config.startupTimeout(), is(42L));
    }

    @Test
    public void startup_timeout_has_a_default_value() {
        DaemonConfiguration config = defaultConfig();

        assertThat(config.startupTimeout(), is(DaemonConfiguration.DEFAULT_STARTUP_TIMEOUT));
    }

    @Test
    public void idle_timeout_can_be_changed() {
        DaemonConfiguration original = new DaemonConfigurationBuilder()
                .idleTimeout(42L)
                .build();

        DaemonConfiguration converted = parseSuiteOptions(original);

        assertThat(converted.idleTimeout(), is(42L));
    }

    @Test
    public void idle_timeout_has_a_default_value() {
        DaemonConfiguration config = defaultConfig();

        assertThat(config.idleTimeout(), is(DaemonConfiguration.DEFAULT_IDLE_TIMEOUT));
    }


    // helpers

    private static DaemonConfiguration parseSuiteOptions(DaemonConfiguration daemonConfiguration) {
        return DaemonConfigurationConverter.parse(dummyArgs(), toProperties(DaemonConfigurationConverter.toSystemProperties(daemonConfiguration)));
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

    private DaemonConfiguration parseSystemProperties(String key, String value) {
        Properties p = new Properties();
        p.setProperty(key, value);
        return DaemonConfigurationConverter.parse(dummyArgs(), p);
    }

    private DaemonConfiguration defaultConfig() {
        return DaemonConfigurationConverter.parse(dummyArgs(), new Properties());
    }

    private static String[] dummyArgs() {
        int launcherPort = new Random().nextInt(100) + 1;
        return new String[]{DaemonConfigurationConverter.LAUNCHER_PORT, "" + launcherPort};
    }
}
