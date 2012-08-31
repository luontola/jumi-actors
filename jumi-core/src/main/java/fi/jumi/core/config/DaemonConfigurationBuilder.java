// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class DaemonConfigurationBuilder {

    private int launcherPort = -1;
    private boolean logActorMessages = DaemonConfiguration.DEFAULT_LOG_ACTOR_MESSAGES;
    private long startupTimeout = DaemonConfiguration.DEFAULT_STARTUP_TIMEOUT;
    private long idleTimeout = DaemonConfiguration.DEFAULT_IDLE_TIMEOUT;

    public int launcherPort() {
        return launcherPort;
    }

    public DaemonConfigurationBuilder launcherPort(int launcherPort) {
        this.launcherPort = launcherPort;
        return this;
    }

    public boolean logActorMessages() {
        return logActorMessages;
    }

    public DaemonConfigurationBuilder logActorMessages(boolean logActorMessages) {
        this.logActorMessages = logActorMessages;
        return this;
    }

    public long startupTimeout() {
        return startupTimeout;
    }

    public DaemonConfigurationBuilder startupTimeout(long startupTimeout) {
        this.startupTimeout = startupTimeout;
        return this;
    }

    public long idleTimeout() {
        return idleTimeout;
    }

    public DaemonConfigurationBuilder idleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public DaemonConfiguration build() {
        return new DaemonConfiguration(this);
    }
}
