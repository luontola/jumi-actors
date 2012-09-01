// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Immutable
public class DaemonConfiguration {

    public static final boolean DEFAULT_LOG_ACTOR_MESSAGES = false;
    public static final long DEFAULT_STARTUP_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    public static final long DEFAULT_IDLE_TIMEOUT = TimeUnit.SECONDS.toMillis(5); // TODO: increase to 15 min, after implementing persistent daemons

    private final int launcherPort;
    private final boolean logActorMessages;
    private final long startupTimeout;
    private final long idleTimeout;

    public DaemonConfiguration() {
        launcherPort = -1;
        logActorMessages = DEFAULT_LOG_ACTOR_MESSAGES;
        startupTimeout = DEFAULT_STARTUP_TIMEOUT;
        idleTimeout = DEFAULT_IDLE_TIMEOUT;
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

    public String[] toProgramArgs() {
        return new String[]{DaemonConfigurationConverter.LAUNCHER_PORT, String.valueOf(launcherPort())};
    }

    public Map<String, String> toSystemProperties() {
        return DaemonConfigurationConverter.toSystemProperties(this);
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
