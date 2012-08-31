// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConfigurationTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();


    // command line arguments

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


    // system properties

    @Test
    public void logging_actor_messages_can_be_enabled() {
        Configuration config = parseSystemProperties(Configuration.LOG_ACTOR_MESSAGES, "true");

        assertThat(config.logActorMessages, is(true));
    }

    @Test
    public void logging_actor_messages_defaults_to_disabled() {
        Configuration config = defaultConfig();

        assertThat(config.logActorMessages, is(false));
    }

    @Test
    public void startup_timeout_can_be_changed() {
        SuiteConfiguration suiteConfig = new SuiteConfigurationBuilder()
                .setStartupTimeout(42L)
                .build();

        Configuration config = parseSuiteOptions(suiteConfig);

        assertThat(config.startupTimeout, is(42L));
    }

    @Test
    public void startup_timeout_has_a_default_value() {
        Configuration config = defaultConfig();

        assertThat(config.startupTimeout, is(Configuration.DEFAULT_STARTUP_TIMEOUT));
    }

    @Test
    public void idle_timeout_can_be_changed() {
        SuiteConfiguration suiteConfig = new SuiteConfigurationBuilder()
                .setIdleTimeout(42L)
                .build();

        Configuration config = parseSuiteOptions(suiteConfig);

        assertThat(config.idleTimeout, is(42L));
    }

    @Test
    public void idle_timeout_has_a_default_value() {
        Configuration config = defaultConfig();

        assertThat(config.idleTimeout, is(Configuration.DEFAULT_IDLE_TIMEOUT));
    }


    // helpers

    private static Configuration parseSuiteOptions(SuiteConfiguration configuration) {
        return Configuration.parse(dummyArgs(), toProperties(configuration.toDaemonSystemProperties()));
    }

    private static Properties toProperties(Map<String, String> map) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    private Configuration parseArgs(String... args) {
        return Configuration.parse(args, new Properties());
    }

    private Configuration parseSystemProperties(String key, String value) {
        Properties p = new Properties();
        p.setProperty(key, value);
        return Configuration.parse(dummyArgs(), p);
    }

    private Configuration defaultConfig() {
        return Configuration.parse(dummyArgs(), new Properties());
    }

    private static String[] dummyArgs() {
        int launcherPort = new Random().nextInt(100) + 1;
        return new String[]{Configuration.LAUNCHER_PORT, "" + launcherPort};
    }
}
