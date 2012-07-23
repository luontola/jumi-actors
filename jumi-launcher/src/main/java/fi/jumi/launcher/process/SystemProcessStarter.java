// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.process;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;

@Immutable
public class SystemProcessStarter implements ProcessStarter {

    @Override
    public Process startJavaProcess(JvmArgs jvmArgs) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(jvmArgs.getWorkingDir());
        builder.redirectErrorStream(true);
        builder.command(jvmArgs.toCommand());
        return builder.start();
    }
}
