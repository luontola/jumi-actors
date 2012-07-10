// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.process;

import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.util.*;

@Immutable
public class SystemProcessLauncher implements ProcessLauncher {

    private final File javaExecutable = new File(System.getProperty("java.home"), "bin/java");

    @Override
    public Process startJavaProcess(File workingDir, List<String> jvmOptions, File executableJar, String... args) throws IOException {
        List<String> command = new ArrayList<String>();
        command.add(javaExecutable.getAbsolutePath());
        command.addAll(jvmOptions);
        command.add("-jar");
        command.add(executableJar.getAbsolutePath());
        command.addAll(Arrays.asList(args));

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(workingDir);
        builder.redirectErrorStream(true);
        builder.command(command);
        return builder.start();
    }
}
