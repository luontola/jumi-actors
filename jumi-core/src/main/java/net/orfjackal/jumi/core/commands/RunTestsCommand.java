// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core.commands;

import net.orfjackal.jumi.core.CommandListener;

import java.io.*;
import java.util.List;

public class RunTestsCommand implements Command, Serializable {
    private final List<File> classPath;
    private final String testsToIncludePattern;

    public RunTestsCommand(List<File> classPath, String testsToIncludePattern) {
        this.classPath = classPath;
        this.testsToIncludePattern = testsToIncludePattern;
    }

    public void fireOn(CommandListener target) {
        target.runTests(classPath, testsToIncludePattern);
    }
}
