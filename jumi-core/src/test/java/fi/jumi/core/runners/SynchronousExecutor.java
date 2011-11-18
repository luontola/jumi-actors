// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import java.util.concurrent.Executor;

public class SynchronousExecutor implements Executor {
    public void execute(Runnable command) {
        command.run();
    }
}
