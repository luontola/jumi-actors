// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.process;

import fi.jumi.core.util.Immutables;

import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.util.*;

@Immutable
public class JvmArgs {

    // TODO: make private, or keep public to ease testing?

    public final File workingDir;
    public final File javaHome;
    public final List<String> jvmOptions;
    public final Map<String, String> systemProperties;
    public final File executableJar;
    public final List<String> programArgs;

    public JvmArgs(File workingDir,
                   File javaHome,
                   List<String> jvmOptions,
                   Map<String, String> systemProperties,
                   File executableJar,
                   String[] programArgs) {
        this.workingDir = workingDir;
        this.javaHome = javaHome;
        this.jvmOptions = Immutables.list(jvmOptions);
        this.systemProperties = Immutables.map(systemProperties);
        this.executableJar = executableJar;
        this.programArgs = Immutables.list(programArgs);
    }

    public File getWorkingDir() {
        return workingDir;
    }

    public List<String> toCommand() {
        List<String> command = new ArrayList<String>();
        command.add(new File(javaHome, "bin/java").getAbsolutePath());
        command.addAll(jvmOptions);
        command.addAll(asJvmOptions(systemProperties));
        command.add("-jar");
        command.add(executableJar.getAbsolutePath());
        command.addAll(programArgs);
        return command;
    }

    private static List<String> asJvmOptions(Map<String, String> systemProperties) {
        List<String> jvmOptions = new ArrayList<String>();
        for (Map.Entry<String, String> p : systemProperties.entrySet()) {
            jvmOptions.add("-D" + p.getKey() + "=" + p.getValue());
        }
        return jvmOptions;
    }
}
