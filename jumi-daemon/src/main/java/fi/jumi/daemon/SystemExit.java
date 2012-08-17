// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import javax.annotation.concurrent.Immutable;

@Immutable
public class SystemExit implements Runnable {

    private final String message;

    public SystemExit(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println("The system will now exit: " + message);
        System.exit(0);
    }
}
