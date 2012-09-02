// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class DaemonConfigurationBuilder {

    private int launcherPort;
    private boolean logActorMessages;
    private long startupTimeout;
    private long idleTimeout;

    public DaemonConfigurationBuilder() {
        this(DaemonConfiguration.DEFAULTS);
    }

    DaemonConfigurationBuilder(DaemonConfiguration src) {
        launcherPort = src.launcherPort();
        logActorMessages = src.logActorMessages();
        startupTimeout = src.startupTimeout();
        idleTimeout = src.idleTimeout();
    }

    public DaemonConfiguration freeze() {
        return new DaemonConfiguration(this);
    }


    // conversions

    public DaemonConfigurationBuilder parseProgramArgs(String... args) {
        // TODO: extract generic code, once we have two or more command line arguments
        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String parameter = it.next();
            if (parameter.equals(DaemonConfiguration.LAUNCHER_PORT)) {
                launcherPort(Integer.parseInt(it.next()));
            } else {
                throw new IllegalArgumentException("unsupported parameter: " + parameter);
            }
        }
        checkRequiredParameters();
        return this;
    }

    private void checkRequiredParameters() {
        if (launcherPort() <= 0) {
            throw new IllegalArgumentException("missing required parameter: " + DaemonConfiguration.LAUNCHER_PORT);
        }
    }

    public DaemonConfigurationBuilder parseSystemProperties(Properties systemProperties) {
        for (SystemProperty property : DaemonConfiguration.PROPERTIES) {
            property.parseSystemProperty(this, systemProperties);
        }
        return this;
    }


    // getters and setters

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
}
