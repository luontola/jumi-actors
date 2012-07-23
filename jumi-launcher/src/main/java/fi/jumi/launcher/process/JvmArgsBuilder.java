// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.process;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.util.*;

@NotThreadSafe
public class JvmArgsBuilder {

    private File executableJar;
    private File workingDir;
    private List<String> jvmOptions;
    private Properties systemProperties;
    private File javaHome = new File(System.getProperty("java.home"));
    private String[] programArgs;

    public JvmArgsBuilder executableJar(File executableJar) {
        this.executableJar = executableJar;
        return this;
    }

    public JvmArgsBuilder workingDir(File workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    public JvmArgsBuilder jvmOptions(List<String> jvmOptions) {
        this.jvmOptions = jvmOptions;
        return this;
    }

    public JvmArgsBuilder systemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
        return this;
    }

    public JvmArgsBuilder setJavaHome(File javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    public JvmArgsBuilder programArgs(String... programArgs) {
        this.programArgs = programArgs;
        return this;
    }

    public JvmArgs toJvmArgs() {
        return new JvmArgs(executableJar, workingDir, jvmOptions, systemProperties, javaHome, programArgs);
    }
}