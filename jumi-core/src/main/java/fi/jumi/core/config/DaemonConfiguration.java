// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Immutable
public class DaemonConfiguration {

    public static final DaemonConfiguration DEFAULTS = new DaemonConfiguration();

    // command line arguments
    // TODO: generic way for representing command line arguments, after we have two or more arguments
    public static final String LAUNCHER_PORT = "--launcher-port";

    public static final SystemProperty IDLE_TIMEOUT = new SystemProperty("idleTimeout", "jumi.daemon.idleTimeout", DEFAULTS);
    public static final SystemProperty STARTUP_TIMEOUT = new SystemProperty("startupTimeout", "jumi.daemon.startupTimeout", DEFAULTS);
    public static final SystemProperty LOG_ACTOR_MESSAGES = new SystemProperty("logActorMessages", "jumi.daemon.logActorMessages", DEFAULTS);
    public static final List<SystemProperty> PROPERTIES = Arrays.asList(LOG_ACTOR_MESSAGES, STARTUP_TIMEOUT, IDLE_TIMEOUT);

    private final int launcherPort;
    private final boolean logActorMessages;
    private final long startupTimeout;
    private final long idleTimeout;

    public DaemonConfiguration() {
        launcherPort = 0;
        logActorMessages = false;
        startupTimeout = TimeUnit.SECONDS.toMillis(30);
        idleTimeout = TimeUnit.SECONDS.toMillis(5);  // TODO: increase to 15 min, after implementing persistent daemons
    }

    DaemonConfiguration(DaemonConfigurationBuilder src) {
        launcherPort = src.launcherPort();
        logActorMessages = src.logActorMessages();
        startupTimeout = src.startupTimeout();
        idleTimeout = src.idleTimeout();
    }

    public DaemonConfigurationBuilder melt() {
        return new DaemonConfigurationBuilder(this);
    }


    // conversions

    public String[] toProgramArgs() {
        return new String[]{LAUNCHER_PORT, String.valueOf(launcherPort())};
    }

    public Map<String, String> toSystemProperties() {
        Map<String, String> map = new HashMap<String, String>();
        for (SystemProperty property : PROPERTIES) {
            property.toSystemProperty(this, map);
        }
        return map;
    }


    // getters

    public int launcherPort() {
        return launcherPort;
    }

    public boolean logActorMessages() {
        return logActorMessages;
    }

    public long startupTimeout() {
        return startupTimeout;
    }

    public long idleTimeout() {
        return idleTimeout;
    }
}
