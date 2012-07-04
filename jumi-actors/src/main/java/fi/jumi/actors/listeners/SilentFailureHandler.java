// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.listeners;

import javax.annotation.concurrent.Immutable;

@Immutable
public class SilentFailureHandler implements FailureHandler {

    @Override
    public void uncaughtException(Object actor, Object message, Throwable exception) {
    }
}
