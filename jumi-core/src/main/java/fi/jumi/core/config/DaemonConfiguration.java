// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Immutable
public class DaemonConfiguration {

    // default values
    public static final boolean DEFAULT_LOG_ACTOR_MESSAGES = false;
    public static final long DEFAULT_STARTUP_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    public static final long DEFAULT_IDLE_TIMEOUT = TimeUnit.SECONDS.toMillis(5); // TODO: increase to 15 min, after implementing persistent daemons

    private final boolean logActorMessages;
    private final int launcherPort;
    private final long startupTimeout;
    private final long idleTimeout;

    DaemonConfiguration(DaemonConfigurationBuilder builder) {
        logActorMessages = builder.logActorMessages();
        launcherPort = builder.launcherPort();
        startupTimeout = builder.startupTimeout();
        idleTimeout = builder.idleTimeout();
    }

    public String[] toProgramArgs() {
        return new String[]{DaemonConfigurationConverter.LAUNCHER_PORT, String.valueOf(launcherPort())};
    }

    public Map<String, String> toSystemProperties() {
        return DaemonConfigurationConverter.toSystemProperties(this);
    }

    public boolean logActorMessages() {
        return logActorMessages;
    }

    public int launcherPort() {
        return launcherPort;
    }

    public long startupTimeout() {
        return startupTimeout;
    }

    public long idleTimeout() {
        return idleTimeout;
    }
}
