// Copyright © 2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import java.util.Queue;
import java.util.concurrent.*;

public class AsynchronousExecutor implements Executor {

    private final Queue<Runnable> commands = new ConcurrentLinkedQueue<Runnable>();

    public void execute(Runnable command) {
        commands.add(command);
    }

    public void runUntilIdle() {
        while (true) {
            Runnable command = commands.poll();
            if (command == null) {
                return;
            }
            command.run();
        }
    }
}
